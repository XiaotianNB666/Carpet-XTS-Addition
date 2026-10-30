package cn.gcte.xts.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class PlayerCompat {
    public static void startSleepInBed(ServerPlayer player, BlockPos pos) {
        player.startSleepInBed(pos);
    }
}
