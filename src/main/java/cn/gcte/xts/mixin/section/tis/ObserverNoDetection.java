package cn.gcte.xts.mixin.section.tis;

import cn.gcte.xts.features.section.SectionManager;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.ObserverBlock;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded("carpet-tis-addition")
@Mixin(value = ObserverBlock.class, priority = 1100)
public class ObserverNoDetection {
    @Inject(method = "startSignal", at = @At("HEAD"))
    private void startSignal(@NotNull LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, CallbackInfo ci) {
        if (! (SectionManager.enabled() && SectionManager.getRuleValue(blockPos, ((Level) levelReader).dimension(), "observerNoDetection", false))) {
            var self = (ObserverBlock) (Object) this;
        if (!levelReader.isClientSide() && !scheduledTickAccess.getBlockTicks().hasScheduledTick(blockPos, self)) {
            scheduledTickAccess.scheduleTick(blockPos, self, 2);
        }
        }
    }
}
