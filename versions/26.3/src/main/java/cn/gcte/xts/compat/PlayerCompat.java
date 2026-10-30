package cn.gcte.xts.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.AbstractBedBlock;
import org.jetbrains.annotations.NotNull;

public class PlayerCompat {
    public static void startSleepInBed(@NotNull ServerPlayer player, BlockPos pos) {
        var level = player.level();
        var blockState = level.getBlockState(pos);
        if (blockState.getBlock() instanceof AbstractBedBlock bedBlock) {
            player.startSleepInBed(bedBlock, blockState, bedBlock.getBedRule(level, pos), pos);
        }
    }
}
