package cn.gcte.xts.features.restart;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        } else if (mainAndArgs != null && !mainAndArgs.isEmpty()) {
            List<String> tokens = splitCommand(mainAndArgs);
            String first = tokens.getFirst();
            if (isJarFile(first)) {
                cmd.add("-jar");
                cmd.add(resolveJar(first));
                cmd.addAll(tokens.subList(1, tokens.size()));
            } else if (classpath != null && !classpath.isEmpty()) {
                cmd.add("-cp");
                cmd.add(classpath);
                cmd.addAll(tokens);
            } else {
                throw new IllegalStateException(
                        "sun.java.command is a class but no classpath available");
            }
        } else {
            throw new IllegalStateException(
                    "no -jar and no sun.java.command available");
        }
        return List.copyOf(cmd);
    }

    @NotNull
    static @Unmodifiable List<String> buildScriptCommand(Path script) {
        if (windows()) {
            return List.of("cmd", "/c", script.toAbsolutePath().toString());
        }
        return List.of("sh", script.toAbsolutePath().toString());
    }

    @NotNull
    private static String javaExecutable() {
        String home = System.getProperty("java.home");
        String exe = windows() ? "bin\\java.exe" : "bin/java";
        return Paths.get(home, exe).toString();
    }

    @NotNull
    @Unmodifiable
    private static List<String> filteredJvmArgs() {
        List<String> raw = runtimeMxBean().getInputArguments();
        List<String> out = new ArrayList<>(raw.size());
        for (String a : raw) {
            if (a.startsWith("-agentlib:")) continue;
            if (a.startsWith("-javaagent:")) continue;
            if (a.startsWith("-Djb.")) continue;
            if (a.startsWith("-Xrun")) continue;
            if (a.startsWith("-Duser.variant")) continue;
            out.add(a);
        }
        return List.copyOf(out);
    }

    private static String findJarArgument() {
        List<String> args = runtimeMxBean().getInputArguments();
        for (int i = 0; i < args.size(); i++) {
            if ("-jar".equals(args.get(i)) && i + 1 < args.size()) {
                String jar = args.get(i + 1);
                return resolveJar(jar);
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

    @VisibleForTesting
    @NotNull
    @Unmodifiable
    static List<String> splitCommand(@NotNull String command) {
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

    @Contract(pure = true)
    private static boolean isJarFile(String s) {
        return s.endsWith(".jar");
    }

    private static @NotNull String resolveJar(String jar) {
        return Paths.get(System.getProperty("user.dir")).resolve(jar).normalize().toString();
    }

    private static boolean windows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}