package cn.gcte.xts.mixin.tp;

import cn.gcte.xts.CarpetXTSAddition;
import cn.gcte.xts.features.tpcollection.LastPositionManager;
import cn.gcte.xts.features.tpcollection.PositionRecord;
import cn.gcte.xts.features.tpcollection.TPCollection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.portal.TeleportTransition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerTPMixin {
    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"))
    private void teleport(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir) {
        if (CarpetXTSAddition.TempSharedConstants.everyTPCanBack) {
            LastPositionManager.setLast((ServerPlayer) (Object) this);
        }
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void die(DamageSource damageSource, CallbackInfo ci) {
        if (! CarpetXTSAddition.TempSharedConstants.everyTPCanBack) LastPositionManager.setLast((ServerPlayer) (Object) this);
    }
}
