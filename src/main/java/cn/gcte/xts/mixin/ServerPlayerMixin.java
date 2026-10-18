package cn.gcte.xts.mixin;

import cn.gcte.xts.rules.XTSRules;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin {

    @Shadow public abstract void sendAbilitiesUpdate();

    @Unique private int xts$flightFuel = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void xts$creativeFlightWithRockets(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        if (!XTSRules.creativeFlight() || !self.interactionManager.isSurvivalLike()) {
            if (self.getAbilities().flying && !self.isCreative() && !self.isSpectator()) {
                xts$stopFlying(self, false);
            } else {
                self.getAbilities().allowFlying = self.isCreative() || self.isSpectator();
            }
            return;
        }

        boolean hasElytra = self.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
        int slot = findPlainRocketSlot(self);

        var applyFallDamage = XTSRules.creativeFlightFallHasDamage() && !self.isCreative() && !self.isSpectator();

        if (!hasElytra || slot < 0) {
            if (self.getAbilities().flying) {
                xts$stopFlying(self, applyFallDamage);
            } else {
                self.getAbilities().allowFlying = false;
            }
            return;
        }

        self.getAbilities().allowFlying = true;

        if (self.getAbilities().flying) {
            if (xts$flightFuel <= 0) {
                if (consumeOneRocket(self, slot) && damageElytra(self)) {
                    xts$flightFuel = boostTicksFor(self.getInventory().getStack(slot));
                } else {
                    xts$stopFlying(self, applyFallDamage);
                }
            } else {
                xts$flightFuel--;
            }
        } else {
            xts$flightFuel = boostTicksFor(self.getInventory().getStack(slot));
        }

        sendAbilitiesUpdate();
    }

    @Unique
    private void xts$stopFlying(ServerPlayerEntity p, boolean applyFall) {
        p.getAbilities().flying = false;
        p.getAbilities().allowFlying = false;

        if (applyFall) {
            Vec3d v = p.getVelocity();
            if (v.y > 0) p.setVelocity(v.x, Math.min(0, v.y), v.z);
        }

        p.sendAbilitiesUpdate();
    }

    @Unique
    private int findPlainRocketSlot(ServerPlayerEntity p) {
        PlayerInventory inv = p.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.getItem() != Items.FIREWORK_ROCKET) continue;
            FireworksComponent fw = s.get(DataComponentTypes.FIREWORKS);
            if (fw == null || fw.explosions().isEmpty()) return i;
        }
        return -1;
    }

    @Unique
    private int rocketFlightLevel(ItemStack stack) {
        if (stack.getItem() != Items.FIREWORK_ROCKET) return -1;
        FireworksComponent fw = stack.get(DataComponentTypes.FIREWORKS);
        if (fw == null) return 1;              // no component → Duration 1
        return fw.flightDuration() + 1;        // 0→1, 1→2, 2→3
    }

    @Unique
    private int boostTicksFor(ItemStack stack) {
        int lvl = rocketFlightLevel(stack);
        if (lvl < 1) lvl = 1;
        int base = XTSRules.creativeFlightFuelBase();
        return base * (2 + lvl);
    }

    @Unique
    private boolean consumeOneRocket(ServerPlayerEntity p, int slot) {
        PlayerInventory inv = p.getInventory();
        ItemStack s = inv.getStack(slot);
        if (s.isEmpty() || s.getItem() != Items.FIREWORK_ROCKET) return false;
        s.decrement(1);
        if (s.isEmpty()) inv.setStack(slot, ItemStack.EMPTY);
        return true;
    }

    @Unique
    private boolean damageElytra(ServerPlayerEntity p) {
        ItemStack chest = p.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.getItem() != Items.ELYTRA) return false;
        chest.damage(1, p);
        return true;
    }
}