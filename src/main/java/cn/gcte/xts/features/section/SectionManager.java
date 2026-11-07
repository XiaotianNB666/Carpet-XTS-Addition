package cn.gcte.xts.features.section;

import cn.gcte.xts.XTSExtensionServer;
import cn.gcte.xts.rules.XTSRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SectionManager {
    private final Map<String, Set<Section>> ruleToSections = new HashMap<>();
    private static final SectionManager instance = new SectionManager();

    @SuppressWarnings("unchecked")
    public static <T> T getRuleValue(double x, double y, double z, ResourceKey<Level> dimension, String rule, T defaultValue) {
        var may = SectionManager.getInstance().ruleToSections.computeIfAbsent(
                rule, k -> new HashSet<>()
        ).stream().filter(
                section -> section.contains(x, y, z, dimension)
        ).sorted().findFirst();
        if (may.isPresent()) {
            return (T) may.get().rule(rule).value();
        } else {
            var may0 = XTSExtensionServer.settingsManager().getCarpetRule(rule);
            if (may0 == null) {
                return defaultValue;
            } else {
                return (T) may0.value();
            }
        }
    }

    public static <T> T getRuleValue(@NotNull BlockPos blockPos, ResourceKey<Level> dimension, String rule, T defaultValue) {
        return getRuleValue(blockPos.getX(), blockPos.getY(), blockPos.getZ(), dimension, rule, defaultValue);
    }

    public static boolean enabled() {
        return XTSRules.enabledSections();
    }

    public Set<Section> getSections() {
        Set<Section> sections = new HashSet<>();
        ruleToSections.forEach((ignored,v) -> {
            sections.addAll(v);
        });
        return sections;
    }

    static SectionManager getInstance() {
        return instance;
    }

    protected void add(@NotNull Section section) {
        section.rules().forEach(
                rule -> ruleToSections.computeIfAbsent(rule, k -> new HashSet<>()).add(section)
        );
    }

    protected void remove(@NotNull Section section) {
        section.rules().forEach(
                rule -> {
                    Set<Section> sections = ruleToSections.get(rule);
                    if (sections != null) {
                        sections.remove(section);
                    }
                }
        );
        section.remove();
    }
}
