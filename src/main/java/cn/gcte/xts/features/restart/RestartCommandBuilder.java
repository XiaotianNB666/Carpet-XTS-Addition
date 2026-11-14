package cn.gcte.xts.features.restart;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reconstructs the current JVM command line so the server can restart itself
 * when no restartScriptPath is configured.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>{@code -jar &lt;file&gt;} from input arguments (most Fabric servers)</li>
 *   <li>{@code sun.java.command} system property</li>
 * </ol>
 */
final class RestartCommandBuilder {

    private RestartCommandBuilder() {}

    @NotNull
    @Unmodifiable
    static List<String> build() {
        String javaBin = javaExecutable();
        List<String> jvmArgs = filteredJvmArgs();

        String classpath = runtimeMxBean().getClassPath();
        String mainAndArgs = systemProperty();
        String jar = findJarArgument();

        List<String> cmd = new ArrayList<>();
        cmd.add(javaBin);
        cmd.addAll(jvmArgs);

        if (jar != null && !jar.isEmpty()) {
            cmd.add("-jar");
            cmd.add(jar);
        } else if (classpath != null && !classpath.isEmpty()
                && mainAndArgs != null && !mainAndArgs.isEmpty()) {
            cmd.add("-cp");
            cmd.add(classpath);
            cmd.addAll(splitCommand(mainAndArgs));
        } else {
            throw new IllegalStateException(
                    "no -jar and no sun.java.command available");
        }
        return List.copyOf(cmd);
    }

    @NotNull
    static List<String> buildScriptCommand(Path script) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return List.of("cmd", "/c", script.toAbsolutePath().toString());
        }
        return List.of(script.toAbsolutePath().toString());
    }

    @NotNull
    private static String javaExecutable() {
        String home = System.getProperty("java.home");
        String os = System.getProperty("os.name", "");
        String exe = os.toLowerCase().contains("win") ? "bin\\java.exe" : "bin/java";
        return Paths.get(home, exe).toString();
    }

    @NotNull
    @Unmodifiable
    private static List<String> filteredJvmArgs() {
        List<String> raw = runtimeMxBean().getInputArguments();
        boolean dev = FabricLoader.getInstance().isDevelopmentEnvironment();
        if (!dev) return List.copyOf(raw);

        List<String> out = new ArrayList<>(raw.size());
        for (String a : raw) {
            if (a.startsWith("-agentlib:")) continue;     // debug agents
            if (a.startsWith("-javaagent:")) continue;    // IDE agents
            if (a.startsWith("-Djb.")) continue;          // IntelliJ vendor options
            if (a.startsWith("-Xrun")) continue;          // legacy agents
            out.add(a);
        }
        return List.copyOf(out);
    }

    /**
     * Locate "-jar xxx.jar" in the original command line, keeping the path verbatim.
     */
    private static String findJarArgument() {
        List<String> args = runtimeMxBean().getInputArguments();
        for (int i = 0; i < args.size(); i++) {
            if ("-jar".equals(args.get(i)) && i + 1 < args.size()) {
                String jar = args.get(i + 1);
                Path p = Paths.get(jar);
                if (!Files.exists(p)) {
                    p = Paths.get(System.getProperty("user.dir")).resolve(jar).normalize();
                }
                return p.toString();
            }
        }
        return null;
    }

    private static String systemProperty() {
        Map<String, String> props = runtimeMxBean().getSystemProperties();
        return props.get("sun.java.command");
    }

    @VisibleForTesting
    static RuntimeMXBean runtimeMxBean() {
        return ManagementFactory.getRuntimeMXBean();
    }

    /**
     * Splits {@code "main.Class arg1 \"arg with space\""} into tokens,
     * preserving quoted groups.
     */
    @VisibleForTesting
    @NotNull
    @Unmodifiable
    static List<String> splitCommand(String command) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (!sb.isEmpty()) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        if (!sb.isEmpty()) tokens.add(sb.toString());
        return List.copyOf(tokens);
    }
}