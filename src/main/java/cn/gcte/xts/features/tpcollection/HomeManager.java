package cn.gcte.xts.features.tpcollection;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HomeManager {

    private static final Map<UUID, HomeData> HOME_CACHE = new HashMap<>();

    public static void onPlayerJoin(ServerPlayer player) {
        HomeSavedData data = HomeSavedData.get(player.level());
        if (data.hasHome(player.getUUID())) {
            HOME_CACHE.put(player.getUUID(), data.getHome(player.getUUID()));
        }
    }

    public static void onPlayerLeave(ServerPlayer player) {
        HOME_CACHE.remove(player.getUUID());
    }

    public static boolean hasHome(UUID uuid) {
        return HOME_CACHE.containsKey(uuid);
    }

    public static HomeData getHome(UUID uuid) {
        return HOME_CACHE.get(uuid);
    }

    public static void setHome(ServerPlayer player) {
        HomeSavedData data = HomeSavedData.get(player.level());
        HomeData home = new HomeData(
                player.position(),
                player.level().dimension().location().toString(),
                player.getYRot(),
                player.getXRot()
        );
        data.setHome(player.getUUID(), home);
        HOME_CACHE.put(player.getUUID(), home);
    }

    public static void removeHome(ServerPlayer player) {
        HomeSavedData data = HomeSavedData.get(player.level());
        data.removeHome(player.getUUID());
        HOME_CACHE.remove(player.getUUID());
    }

    public static boolean teleportToHome(ServerPlayer player) {
        UUID uuid = player.getUUID();
        HomeData home = HOME_CACHE.get(uuid);
        if (home == null) {
            HomeSavedData data = HomeSavedData.get(player.level());
            if (!data.hasHome(uuid)) return false;
            home = data.getHome(uuid);
            HOME_CACHE.put(uuid, home);
        }

        ResourceKey<Level> dim = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(home.dimension())
        );
        ServerLevel target = player.server.getLevel(dim);
        if (target == null) return false;

        player.teleportTo(target,
                home.pos().x, home.pos().y, home.pos().z,
                Set.of(), home.yaw(), home.pitch(), false);
        return true;
    }

    public record HomeData(Vec3 pos, String dimension, float yaw, float pitch) {
    }
}