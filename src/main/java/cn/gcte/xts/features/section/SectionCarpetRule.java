package cn.gcte.xts.features.section;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.InvalidRuleValueException;
import carpet.api.settings.SettingsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SectionCarpetRule<T> implements CarpetRule<T> {
    String name;
    List<Component> extraInfo;
    Collection<String> categories;
    Collection<String> suggestions;
    SettingsManager settingsManager;
    T value;
    boolean canBeToggledClientSide;
    Class<T> type;
    T defaultValue;

    private static final Map<Class<?>, Convertor<?>> CONVERTER_MAP = Map.ofEntries(
            Map.entry(String.class, str -> str),
            Map.entry(Boolean.class, str -> switch (str) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new InvalidRuleValueException("Invalid boolean for rule");
            }),
            num(Integer.class, Integer::parseInt),
            num(Double.class, Double::parseDouble),
            num(Long.class, Long::parseLong),
            num(Float.class, Float::parseFloat)
    );

    @FunctionalInterface
    public interface Convertor<T> {
        T convert(String value) throws InvalidRuleValueException;

    }

    protected SectionCarpetRule(
            String name,
            List<Component> extraInfo,
            Collection<String> categories,
            Collection<String> suggestions,
            SettingsManager settingsManager,
            T value,
            boolean canBeToggledClientSide,
            Class<T> type,
            T defaultValue
    ) {
        this.name = name;
        this.extraInfo = extraInfo;
        this.categories = categories;
        this.suggestions = suggestions;
        this.settingsManager = settingsManager;
        this.value = value;
        this.canBeToggledClientSide = canBeToggledClientSide;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public static <A> SectionCarpetRule<A> copyFrom(@NotNull CarpetRule<A> other) {
        return new SectionCarpetRule<>(
                other.name(),
                other.extraInfo(),
                other.categories(),
                other.suggestions(),
                other.settingsManager(),
                other.value(),
                other.canBeToggledClientSide(),
                other.type(),
                other.defaultValue()
        );
    }

    @Contract(pure = true)
    static <C> Map. @Unmodifiable @NotNull Entry<Class<C>, Convertor<C>> num(Class<C> clazz, Function<String, C> converter) {
        return Map.entry(clazz, str -> {
            try {
                return converter.apply(str);
            } catch (Exception e) {
                throw new InvalidRuleValueException("Invalid number for rule");
            }
        });
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<Component> extraInfo() {
        return extraInfo;
    }

    @Override
    public Collection<String> categories() {
        return categories;
    }

    @Override
    public Collection<String> suggestions() {
        return suggestions;
    }

    @Override
    public SettingsManager settingsManager() {
        return settingsManager;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public boolean canBeToggledClientSide() {
        return canBeToggledClientSide;
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public T defaultValue() {
        return defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(CommandSourceStack source, String value) throws InvalidRuleValueException {
        set(source, (T) CONVERTER_MAP.get(type()).convert(value));
    }

    @Override
    public void set(CommandSourceStack source, T value) throws InvalidRuleValueException {
        this.value = value;
    }
}
