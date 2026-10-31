package cn.gcte.xts.mixin.creativefight;

import cn.gcte.xts.features.creativefight.XTSFuelAccess;
import cn.gcte.xts.rules.XTSRules;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements XTSFuelAccess {

    @Shadow public abstract void onUpdateAbilities();

    @Unique private int xts$flightFuel = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void xts$creativeFlightWithRockets(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;

        if (!XTSRules.creativeFlight()) {
            if (self.getAbilities().flying && self.gameMode.isSurvival()) {
                xts$stopFlying(self, XTSRules.creativeFlightFallHasDamage());
            } else {
                self.getAbilities().mayfly = self.isCreative() || self.isSpectator();
            }
            return;
        }

        boolean hasElytra = self.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
        int slot = findPlainRocketSlot(self);

        var applyFallDamage = XTSRules.creativeFlightFallHasDamage() && !self.isCreative() && !self.isSpectator();

        if (!hasElytra || slot < 0) {
            if (self.gameMode().isSurvival()) {
                if (self.getAbilities().flying) {
                    xts$stopFlying(self, applyFallDamage);
                } else {
                    self.getAbilities().mayfly = false;
                }
            }
            return;
        }

        self.getAbilities().mayfly = true;

        if (self.getAbilities().flying && self.gameMode().isSurvival()) {
            if (xts$flightFuel <= 0) {
                if (consumeOneRocket(self, slot) && damageElytra(self)) {
                    xts$flightFuel = boostTicksFor(self.getInventory().getItem(slot));
                } else {
                    xts$stopFlying(self, applyFallDamage);
                }
            } else {
                xts$flightFuel--;
            }
        } else {
            xts$flightFuel = boostTicksFor(self.getInventory().getItem(slot));
        }

        onUpdateAbilities();
    }

    @Unique
    private void xts$stopFlying(ServerPlayer p, boolean applyFall) {
        p.getAbilities().flying = false;
        p.getAbilities().mayfly = false;

        if (applyFall) {
            Vec3 v = p.getDeltaMovement();
            if (v.y > 0) p.setDeltaMovement(v.x, Math.min(0, v.y), v.z);
        }

        p.onUpdateAbilities();
    }

    @Unique
    private int findPlainRocketSlot(ServerPlayer p) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() != Items.FIREWORK_ROCKET) continue;
            Fireworks fw = s.get(DataComponents.FIREWORKS);
            if (fw == null || fw.explosions().isEmpty()) return i;
        }
        return -1;
    }

    @Unique
    private int rocketFlightLevel(ItemStack stack) {
        if (stack.getItem() != Items.FIREWORK_ROCKET) return -1;
        Fireworks fw = stack.get(DataComponents.FIREWORKS);
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
    private boolean consumeOneRocket(ServerPlayer p, int slot) {
        Inventory inv = p.getInventory();
        ItemStack s = inv.getItem(slot);
        if (s.isEmpty() || s.getItem() != Items.FIREWORK_ROCKET) return false;
        s.shrink(1);
        if (s.isEmpty()) inv.setItem(slot, ItemStack.EMPTY);
        return true;
    }

    @Unique
    private boolean damageElytra(ServerPlayer p) {
        ItemStack chest = p.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() != Items.ELYTRA || chest.nextDamageWillBreak()) return false;
        chest.hurtWithoutBreaking(1, p);
        return true;
    }

    @Unique
    public int xts$getFlightFuel() {
        return xts$flightFuel;
    }

    @Unique
    public void xts$setFlightFuel(int fuel) {
        xts$flightFuel = fuel;
    }

}