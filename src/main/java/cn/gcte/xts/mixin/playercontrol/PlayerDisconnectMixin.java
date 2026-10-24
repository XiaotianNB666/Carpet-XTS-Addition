package cn.gcte.xts.mixin.playercontrol;

import cn.gcte.xts.features.playercontrol.PlayerControl;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class PlayerDisconnectMixin {
    @Inject(method = "disconnect", at = @At("HEAD"))
    public void kill(CallbackInfo ci) {
        var player = (ServerPlayer) (Object) this;
        var manager = PlayerControl.getSwapManager();
        if (manager.isSwapped(player.getUUID())) {
            manager.release(player, player.server);
        }
        player.server.getPlayerList().saveAll();
    }
}
