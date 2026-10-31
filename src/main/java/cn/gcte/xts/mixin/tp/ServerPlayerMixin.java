package cn.gcte.xts.mixin.tp;

import cn.gcte.xts.features.tpcollection.PositionRecord;
import cn.gcte.xts.features.tpcollection.TPCollection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements TPCollection.LastPositionAccess {
    @Unique
    private PositionRecord xts$last = null;

    public @Nullable PositionRecord xts$last() {
        return xts$last;
    }

    public void xts$setLast(PositionRecord xts$last) {
        this.xts$last = xts$last;
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("RETURN"))
    private void teleport(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir) {
        xts$setLast(PositionRecord.from(teleportTransition));
    }
}
