package cn.gcte.xts.features.section;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.InvalidRuleValueException;
import cn.gcte.xts.CarpetXTSAddition;
import cn.gcte.xts.XTSExtensionServer;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.gcte.xts.features.section.SectionCarpetRule.copyFrom;

public interface Section extends Comparable<Section> {
    int priority = 16;

    String name();

    boolean hasRule(String carpetRule);

    Collection<String> rules();

    ResourceKey<Level> dimension();

    Codec<? extends Section> codec();

    CarpetRule<?> rule(String ruleName);

    default int priority() {
        return priority;
    }

    default int compareTo(@NotNull Section other) {
        return Integer.compare(this.priority(), other.priority());
    }

    boolean renderInWorld();

    ColorRGBA renderColor();

    boolean contains(double x, double y, double z, ResourceKey<Level> dimension);

    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull Section aabb(String name, AABB aabb, ResourceKey<Level> dimension) {
        return new AABBSection(name, aabb, dimension);
    }

    @Contract("_, _, _, _, _, _, _, _ -> new")
    static @NotNull Section aabb(String name, double x1, double y1, double z1, double x2, double y2, double z2, ResourceKey<Level> dimension) {
        return aabb(name, new AABB(x1, y1, z1, x2, y2, z2), dimension);
    }

    void remove();

    class SimpRuleManager {
        // from carpet mod, simplified
        record ConfigReadResult(Map<String, String> ruleMap, boolean locked) {
        }

        Path source;

        SimpRuleManager(Path source) {
            this.source = source;
            loadRules(source);
        }

        Map<String, SectionCarpetRule<?>> rules = new HashMap<>();

        void addRule(CarpetRule<?> rule) {
            addRule(rule instanceof SectionCarpetRule<?> rule0 ? rule0 : copyFrom(rule));
        }

        void addRule(SectionCarpetRule<?> rule) {
            rules.put(rule.name(), rule);
        }

        void removeRule(String ruleName) {
            rules.remove(ruleName);
        }

        void removeRule(@NotNull CarpetRule<?> rule) {
            rules.remove(rule.name());
        }

        boolean hasRule(String ruleName) {
            return rules.containsKey(ruleName);
        }

        public void loadRules(Path source) {
            ConfigReadResult conf = readSettingsFromConf(source);
            for (Map.Entry<String, String> entry : conf.ruleMap().entrySet()) {
                CarpetRule<?> rule = rules.getOrDefault(entry.getKey(),
                        copyFrom(
                                XTSExtensionServer.settingsManager().getCarpetRule(entry.getKey())
                        )
                );
                if (rule == null) {
                    continue;
                }
                try {
                    rule.set(null, entry.getValue());
                } catch (InvalidRuleValueException e) {
                    // ignore bad values
                }
            }
        }

        public void saveRules(boolean locked) {
            Map<String, String> current = new LinkedHashMap<>();
            for (SectionCarpetRule<?> rule : rules.values()) {
                if (rule.value() != rule.defaultValue()) {
                    current.put(rule.name(), (String) rule.value());
                }
            }

            try (BufferedWriter writer = Files.newBufferedWriter(source, StandardCharsets.UTF_8)) {
                writer.write("# XTS Section Carpet Rules\n");
                writer.write("# Generated at " + Instant.now() + "\n\n");

                if (locked) {
                    writer.write("locked\n\n");
                }

                for (Map.Entry<String, String> entry : current.entrySet()) {
                    writer.write(entry.getKey() + " " + entry.getValue() + "\n");
                }
            } catch (IOException e) {
                // ignore or log
            }
        }

        public void remove() {
            rules.clear();
            try {
                Files.deleteIfExists(source);
            } catch (IOException e) {
                CarpetXTSAddition.LOGGER.error("[Section] failed to remove {}", this.source.getFileName());
            }
        }

        @Contract("_ -> new")
        private @NotNull ConfigReadResult readSettingsFromConf(Path path) {
            Map<String, String> result = new HashMap<>();
            boolean confLocked = false;

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.replaceAll("[\\r\\n]", "").trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    if ("locked".equalsIgnoreCase(line)) {
                        confLocked = true;
                        continue;
                    }
                    String[] fields = line.split("\\s+", 2);
                    if (fields.length == 2) {
                        result.put(fields[0], fields[1]);
                    }
                }
            } catch (IOException e) {
                return new ConfigReadResult(new HashMap<>(), false);
            }

            return new ConfigReadResult(result, confLocked);
        }
    }
}
