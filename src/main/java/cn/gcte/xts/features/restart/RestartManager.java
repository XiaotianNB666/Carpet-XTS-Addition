package cn.gcte.xts.features.restart;

import cn.gcte.xts.mixin.ServerAccessor;
import cn.gcte.xts.rules.XTSRules;
import cn.gcte.xts.util.TimeParser;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static cn.gcte.xts.CarpetXTSAddition.LOGGER;
import static cn.gcte.xts.features.restart.RestartCommandBuilder.buildScriptCommand;

public final class RestartManager {

    private static final String MARKER_FILE = ".xts_pending_restart";

    /** Single-threaded real-time scheduler. Uses wall-clock time, not game ticks. */
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "XTS-Restart-Scheduler");
                t.setDaemon(true);
                return t;
            });

    private static volatile boolean restartScheduled = false;
    private static volatile Instant scheduledAt = null;
    private static volatile ScheduledFuture<?> scheduledTask = null;

    private RestartManager() {}

    @Contract(value = "_, _ -> new", pure = true)
    private static @NotNull MutableComponent t(String key, String fallback) {
        return Component.translatableWithFallback(key, fallback);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    private static @NotNull MutableComponent t(String key, String fallback, Object... args) {
        return Component.translatableWithFallback(key, fallback, args);
    }

    public static boolean isRestartScheduled() {
        return restartScheduled;
    }

    public static Instant getScheduledAt() {
        return scheduledAt;
    }

    /** Schedule a restart after the given real-time delay. */
    public static void scheduleRestart(@NotNull CommandSourceStack source, Duration delay) {
        MinecraftServer server = source.getServer();
        if (!server.isDedicatedServer()) {
            source.sendFailure(t("xts.restart.dedicated_only",
                    "Restart can only be used on a dedicated server"));
            return;
        }
        if (restartScheduled) {
            source.sendFailure(t("xts.restart.already_scheduled",
                    "A restart is already scheduled (use /restart cancel)"));
            return;
        }
        if (delay.isNegative() || delay.toMillis() < 50) {
            runOnServerThread(server, () -> doShutdown(server));
            return;
        }

        restartScheduled = true;
        Instant target = Instant.now().plus(delay);
        scheduledAt = target;

        String script = readScriptPath();
        boolean hasScript = XTSRules.hasRestartScriptPath();

        String mode = hasScript
                ? t("xts.restart.script", "script=%s", script).getString()
                : t("xts.restart.self", "self-restart").getString();

        server.getPlayerList().broadcastSystemMessage(
                t("xts.restart.scheduled_in",
                        "Server restart in %s (%s) at %s",
                        TimeParser.formatDelay(delay), mode,
                        TimeParser.formatInstant(target)),
                false);
        LOGGER.info("Server restart in {} ({}) at {}",
                TimeParser.formatDelay(delay), mode, TimeParser.formatInstant(target));

        writeMarker(server, hasScript, script);

        scheduledTask = SCHEDULER.schedule(
                () -> runOnServerThread(server, () -> doShutdown(server)),
                delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    /** Schedule a restart at an exact wall-clock instant. */
    public static void scheduleRestartAt(@NotNull CommandSourceStack source, Instant target) {
        MinecraftServer server = source.getServer();
        if (!server.isDedicatedServer()) {
            source.sendFailure(t("xts.restart.dedicated_only",
                    "Restart can only be used on a dedicated server"));
            return;
        }
        if (restartScheduled) {
            source.sendFailure(t("xts.restart.already_scheduled",
                    "A restart is already scheduled (use /restart cancel)"));
            return;
        }

        Duration delay = Duration.between(Instant.now(), target);
        if (delay.isNegative() || delay.toMillis() < 50) {
            runOnServerThread(server, () -> doShutdown(server));
            return;
        }

        restartScheduled = true;
        scheduledAt = target;

        String script = readScriptPath();
        boolean hasScript = XTSRules.hasRestartScriptPath();

        String mode = hasScript
                ? t("xts.restart.script", "script=%s", script).getString()
                : t("xts.restart.self", "self-restart").getString();

        server.getPlayerList().broadcastSystemMessage(
                t("xts.restart.scheduled_at",
                        "Server restart scheduled at %s (%s)",
                        TimeParser.formatInstant(target), mode),
                false);
        LOGGER.info("Server restart scheduled at {} ({})",
                TimeParser.formatInstant(target), mode);

        writeMarker(server, hasScript, script);

        scheduledTask = SCHEDULER.schedule(
                () -> runOnServerThread(server, () -> doShutdown(server)),
                delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    /** Cancel a pending scheduled restart. */
    public static boolean cancelRestart(@NotNull CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        if (!restartScheduled) {
            source.sendFailure(t("xts.restart.cancel_none", "No restart is scheduled"));
            return false;
        }
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        scheduledTask = null;
        restartScheduled = false;
        scheduledAt = null;

        clearMarker(server);
        server.getPlayerList().broadcastSystemMessage(
                t("xts.restart.cancelled", "Scheduled restart cancelled"), false);
        LOGGER.info("Scheduled restart cancelled");
        return true;
    }

    private static void runOnServerThread(@NotNull MinecraftServer server, Runnable task) {
        if (server.isSameThread()) {
            task.run();
        } else {
            server.execute(task);
        }
    }

    private static void doShutdown(@NotNull MinecraftServer server) {
        restartScheduled = false;
        scheduledAt = null;
        scheduledTask = null;

        server.getPlayerList().broadcastSystemMessage(
                t("xts.restart.now", "Restarting now..."), false);
        LOGGER.info("Initiating clean shutdown for restart");

        ((ServerAccessor) server).setRunning(false);

        if (XTSRules.hasRestartScriptPath()) {
            launchScript(readScriptPath(), server);
        } else {
            launchSelf(server);
        }
    }

    private static void launchScript(String script, MinecraftServer server) {
        Path p = Path.of(script);
        if (!p.isAbsolute()) {
            p = server.getServerDirectory().resolve(p).normalize();
        }
        if (!Files.exists(p)) {
            LOGGER.error("{}",
                    t("xts.restart.no_script_found",
                            "restartScriptPath '%s' not found, aborting relaunch", p).getString());
            return;
        }
        doLaunch(buildScriptCommand(p), "script: " + p);
    }

    /** No restartScriptPath -> rebuild the current JVM command line. */
    private static void launchSelf(MinecraftServer server) {
        try {
            List<String> cmd = RestartCommandBuilder.build();
            doLaunch(cmd, "self-restart");
        } catch (IllegalStateException e) {
            LOGGER.error("{}",
                    t("xts.restart.rebuild_failed",
                            "Cannot rebuild JVM command line: %s", e.getMessage()).getString());
            LOGGER.error("Set restartScriptPath to a valid script, or run under a supported JVM (HotSpot/OpenJ9)");
        }
    }

    private static void doLaunch(List<String> command, String label) {
        LOGGER.info("Launching ({}) {}", label, String.join(" ", command));
        try {
            new ProcessBuilder(command)
                    .inheritIO()
                    .directory(new File(System.getProperty("user.dir")))
                    .start();
            LOGGER.info("Launched successfully");
        } catch (IOException e) {
            LOGGER.error("Failed to launch ({}): {}", label, e.getMessage());
        }
    }

    private static String readScriptPath() {
        return XTSRules.restartServerScriptPath();
    }

    private static void writeMarker(@NotNull MinecraftServer server, boolean hasScript, String script) {
        try {
            Path marker = server.getServerDirectory().resolve(MARKER_FILE);
            Files.writeString(marker, (hasScript ? "script=" : "self\n") + script);
        } catch (IOException e) {
            LOGGER.warn("{}",
                    t("xts.restart.marker_write_failed", "Could not write restart marker").getString(), e);
        }
    }

    private static void clearMarker(MinecraftServer server) {
        if (server == null) return;
        try {
            Path marker = server.getServerDirectory().resolve(MARKER_FILE);
            Files.deleteIfExists(marker);
        } catch (IOException ignored) {
        }
    }

    /** Shut down the scheduler; call when the server stops. */
    public static void shutdown() {
        SCHEDULER.shutdownNow();
    }
}