package cn.gcte.xts.features.restart;

import cn.gcte.xts.compat.Permission;
import cn.gcte.xts.rules.XTSRules;
import cn.gcte.xts.util.TimeParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class RestartCommand {

    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext ignored) {

        LiteralArgumentBuilder<CommandSourceStack> root = literal("restart")
                .requires(src -> XTSRules.enabledRestartServerCommand(src.getServer())
                        && Permission.hasPermission(src, XTSRules.restartServerCommandLevel()))
                // /restart  -> immediate
                .executes(RestartCommand::runNow)
                // /restart in <duration>
                .then(literal("in")
                        .then(argument("duration", StringArgumentType.word())
                                .executes(ctx -> {
                                    String raw = StringArgumentType.getString(ctx, "duration");
                                    Duration d = TimeParser.parseRelative(raw);
                                    return runDelay(ctx, d);
                                })))
                // /restart at <date> [<time>]
                .then(literal("at")
                        .then(argument("date", StringArgumentType.word())
                                .executes(ctx -> runAbsolute(ctx,
                                        StringArgumentType.getString(ctx, "date"), null))
                                .then(argument("time", StringArgumentType.word())
                                        .executes(ctx -> runAbsolute(ctx,
                                                StringArgumentType.getString(ctx, "date"),
                                                StringArgumentType.getString(ctx, "time"))))));

        root.then(literal("cancel")
                .executes(ctx -> {
                    if (!RestartManager.cancelRestart(ctx.getSource())) {
                        ctx.getSource().sendFailure(Component.translatableWithFallback(
                                "xts.restart.failed_cancel",
                                "Failed to cancel the scheduled restart."
                        ));
                    }
                    return 1;
                }));

        root.then(Commands.literal("status")
                .executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    if (!RestartManager.isRestartScheduled()) {
                        src.sendSuccess(
                                () -> Component.translatableWithFallback(
                                        "xts.restart.no_schedule", "No restart scheduled"),
                                false);
                        return 1;
                    }
                    Instant at = RestartManager.getScheduledAt();
                    Duration left = Duration.between(Instant.now(), at);
                    src.sendSuccess(
                            () -> Component.translatableWithFallback(
                                    "xts.restart.status",
                                    "Restart scheduled at %s (in %s)",
                                    TimeParser.formatInstant(at),
                                    TimeParser.formatDelay(left)),
                            false);
                    return 1;
                }));

        dispatcher.register(root);
    }

    private static int runNow(@NotNull CommandContext<CommandSourceStack> ctx) {
        RestartManager.scheduleRestart(ctx.getSource(), Duration.ZERO);
        return 1;
    }

    private static int runDelay(@NotNull CommandContext<CommandSourceStack> ctx, Duration delay) {
        RestartManager.scheduleRestart(ctx.getSource(), delay);
        return 1;
    }

    private static int runAbsolute(@NotNull CommandContext<CommandSourceStack> ctx,
                                   String date, String time) throws CommandSyntaxException {
        Duration delay = TimeParser.parseAbsolute(date, time);
        Instant target = Instant.now().plus(delay);
        RestartManager.scheduleRestartAt(ctx.getSource(), target);
        return 1;
    }
}