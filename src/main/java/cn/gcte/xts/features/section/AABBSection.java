package cn.gcte.xts.features.section;

import carpet.CarpetServer;
import carpet.api.settings.CarpetRule;
import cn.gcte.xts.compat.IdentifierCompat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;

public class AABBSection implements Section {

    public static final ResourceKey<Level> OVERWORLD = ResourceKey.create(
            Registries.DIMENSION,
            IdentifierCompat.of("minecraft", "overworld")
    );

    public static final ColorRGBA defaultColor = new ColorRGBA(0xffffffff);

    public static final Codec<AABBSection> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.STRING.fieldOf("name").forGetter(AABBSection::name),
                    Codec.INT.fieldOf("priority").forGetter(AABBSection::priority),
                    Vec3.CODEC.fieldOf("min").forGetter(s -> new Vec3(
                            s.section.minX, s.section.minY, s.section.minZ
                    )),
                    Vec3.CODEC.fieldOf("max").forGetter(s -> new Vec3(
                            s.section.maxX, s.section.maxY, s.section.maxZ
                    )),
                    ResourceKey.codec(Registries.DIMENSION)
                            .optionalFieldOf("dimension", OVERWORLD)
                            .forGetter(AABBSection::dimension),
                    Codec.BOOL.fieldOf("renderInWorld").forGetter(AABBSection::renderInWorld),
                    ColorRGBA.CODEC.optionalFieldOf("renderColor", defaultColor).forGetter(AABBSection::renderColor)
            ).apply(inst, (name, priority, min, max, dim, renderInWorld, renderColor) ->
                    new AABBSection(name, priority, new AABB(min, max), dim, renderInWorld, renderColor)
            )
    );

    String name;
    AABB section;
    ResourceKey<Level> dimension;
    boolean renderInWorld;
    ColorRGBA renderColor;
    SimpRuleManager manager;

    int priority;

    private AABBSection(String name, int priority, @NotNull AABB section, @NotNull ResourceKey<Level> dimension, boolean renderInWorld, ColorRGBA renderColor) {
        this.name = name;
        this.priority = priority;
        this.section = section;
        this.dimension = dimension;
        this.renderInWorld = renderInWorld;
        this.renderColor = renderColor;
        this.manager = new SimpRuleManager(
                CarpetServer.minecraft_server.getWorldPath(LevelResource.ROOT).resolve(Path.of("sections", name + ".conf"))
        );

        SectionManager.getInstance().add(this);
    }

    protected AABBSection(String name, @NotNull AABB section, @NotNull ResourceKey<Level> dimension) {
        this(name, Section.priority, section, dimension, false, defaultColor);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean hasRule(String carpetRule) {
        return manager.hasRule(carpetRule);
    }

    @Override
    public Collection<String> rules() {
        return manager.rules.keySet();
    }

    @Override
    public @NotNull ResourceKey<Level> dimension() {
        return dimension;
    }

    @Override
    public Codec<AABBSection> codec() {
        return CODEC;
    }

    @Override
    public CarpetRule<?> rule(String ruleName) {
        return null;
    }

    @Override
    public boolean renderInWorld() {
        return renderInWorld;
    }

    @Override
    public ColorRGBA renderColor() {
        return renderColor;
    }

    @Override
    public boolean contains(double x, double y, double z, ResourceKey<Level> dimension) {
        return dimension().equals(dimension) && section.contains(x, y, z);
    }

    @Override
    public void remove() {
        this.manager.remove();
    }
}
