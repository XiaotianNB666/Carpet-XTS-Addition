package cn.gcte.xts.features.tpcollection;

import cn.gcte.xts.CarpetXTSAddition;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record PositionRecord(Vec3 pos, ResourceKey<Level> dimension, float yaw, float pitch) {
    @Deprecated
    @Contract("_ -> new")
    public static @NotNull PositionRecord from(@NotNull TeleportTransition teleportTransition) {
        return new PositionRecord(
                teleportTransition.position(),
                teleportTransition.newLevel().dimension(),
                teleportTransition.yRot(),
                teleportTransition.xRot());
    }

    @Contract("_ -> new")
    public static @NotNull PositionRecord from(@NotNull ServerPlayer player) {
        return new PositionRecord(
                player.position(),
                player.level().dimension(),
                player.getYRot(),
                player.getXRot()
        );
    }

    public static @NotNull ResourceKey<Level> dimFromString(String dimension) {
        return ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(dimension)
        );
    }

    public @NotNull String dimToString() {
        return dimension.location().toString();
    }

    public boolean teleportPlayer(@NotNull ServerPlayer player) {
        if (! CarpetXTSAddition.TempSharedConstants.everyTPCanBack) LastPositionManager.setLast(player);
        ServerLevel target = player.server.getLevel(dimension());
        if (target == null) return false;

        return player.teleportTo(target,
                pos().x, pos().y, pos().z,
                Set.of(), yaw(), pitch(), false);
    }

}
