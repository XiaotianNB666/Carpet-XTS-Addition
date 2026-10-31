package cn.gcte.xts.mixin.armortrim;

import cn.gcte.xts.rules.XTSRules;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public class PiglinAIMixin {
    @Inject(
            method = "isWearingSafeArmor",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void safeIfArmorHasGold(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (XTSRules.betterArmorTrims()) {
            for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
                ItemStack stack = entity.getItemBySlot(slot);
                ArmorTrim trim = stack.get(DataComponents.TRIM);
                if (trim != null) {
                    var matKey = trim.material().unwrapKey().orElse(null);
                    if (matKey != null && matKey.location().equals(ResourceLocation.withDefaultNamespace("gold"))) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
}
