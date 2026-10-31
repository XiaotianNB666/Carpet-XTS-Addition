package cn.gcte.xts.features.tpcollection;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LastPositionManager {

    static final Map<UUID, PositionRecord> lastPositions = new HashMap<>();

    private LastPositionManager() {}

    public static void setLast(@NotNull ServerPlayer player) {
        setLast(player.getUUID(), PositionRecord.from(player));
    }

    static void setLast(UUID uuid, PositionRecord positionRecord) {
        lastPositions.put(uuid, positionRecord);
    }

    static PositionRecord getLast(UUID uuid) {
        return lastPositions.get(uuid);
    }

    public static @Nullable PositionRecord getLast(@NotNull ServerPlayer player) {
        return getLast(player.getUUID());
    }
}
