package cn.gcte.xts.features.tpcollection;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HomeManager {

    private static final Map<UUID, PositionRecord> HOME_CACHE = new HashMap<>();

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

    public static PositionRecord getHome(UUID uuid) {
        return HOME_CACHE.get(uuid);
    }

    public static void setHome(ServerPlayer player) {
        HomeSavedData data = HomeSavedData.get(player.level());
        PositionRecord home = new PositionRecord(
                player.position(),
                player.level().dimension(),
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
        PositionRecord home = HOME_CACHE.get(uuid);
        if (home == null) {
            HomeSavedData data = HomeSavedData.get(player.level());
            if (!data.hasHome(uuid)) return false;
            home = data.getHome(uuid);
            HOME_CACHE.put(uuid, home);
        }

        return home.teleportPlayer(player);
    }

}