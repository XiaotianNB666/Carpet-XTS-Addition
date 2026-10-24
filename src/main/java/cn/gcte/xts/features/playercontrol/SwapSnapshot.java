package cn.gcte.xts.features.playercontrol;

import cn.gcte.xts.features.creativefight.XTSFuelAccess;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.*;

public class SwapSnapshot {

    public final GameProfile gameProfile;
    public final boolean fallFlying;
    public final boolean wonGame;
    public final ServerStatsCounter stats;
    public final HumanoidArm mainArm;
    public final ServerGamePacketListenerImpl connection;
    //    public final boolean noCulling;
    public final List<UUID> passengers;
    public final UUID vehicle;
    public final RemoteChatSession chatSession;
    public final List<AttributeInstance.Packed> attributes;
    public final Abilities abilities;
    public final ServerLevel level;
    public final double x, y, z;
    public final BlockPos isAsleep;
    public final float yRot, xRot;
    public final List<ItemStack> mainInventory;
    public final List<ItemStack> armorInventory;
    public final List<ItemStack> offhandInventory;
    public final List<ItemStack> enderChest;
    public final int selectedSlot;
    public final float health;
    public final int foodLevel;
    public final float saturationLevel;
    public final float exhaustionLevel;
    public final int experienceLevel;
    public final float experienceProgress;
    public final int totalExperience;
    public final GameType gameMode;
    public final List<MobEffectInstance> effects;
    public final List<SynchedEntityData.DataValue<?>> entityData;
    public final CompoundTag additionalData;

    // xts support
    public final int xts$flightFuel;

    private SwapSnapshot(Builder b) {
        this.gameProfile = b.gameProfile;
        this.attributes = b.packedAttributes;
        this.level = b.level;
        this.x = b.x;
        this.y = b.y;
        this.z = b.z;
        this.yRot = b.yRot;
        this.xRot = b.xRot;
        this.mainInventory = b.mainInventory;
        this.armorInventory = b.armorInventory;
        this.offhandInventory = b.offhandInventory;
        this.enderChest = b.enderChest;
        this.selectedSlot = b.selectedSlot;
        this.health = b.health;
        this.foodLevel = b.foodLevel;
        this.saturationLevel = b.saturationLevel;
        this.exhaustionLevel = b.exhaustionLevel;
        this.experienceLevel = b.experienceLevel;
        this.experienceProgress = b.experienceProgress;
        this.totalExperience = b.totalExperience;
        this.gameMode = b.gameMode;
        this.effects = b.effects;
        this.fallFlying = b.fallFlying;
        this.wonGame = b.wonGame;
        this.stats = b.stats;
        this.mainArm = b.mainArm;
        this.connection = b.connection;
//        this.noCulling = b.noCulling;
        this.chatSession = b.chatSession;
        this.abilities = b.abilities;
        this.isAsleep = b.isAsleep;
        this.vehicle = b.vehicle;
        this.passengers = b.passengers;
        this.entityData = b.entityData;
        this.additionalData = b.additionalData;

        this.xts$flightFuel = b.xts$flightFuel;
    }

    public static SwapSnapshot capture(ServerPlayer player) {
        Builder b = new Builder();

        b.fallFlying = player.isFallFlying();
        b.wonGame = player.wonGame;
        b.stats = player.getStats();
        b.mainArm = player.getMainArm();
        b.connection = player.connection;
//        b.noCulling = player.noCulling;
        b.chatSession = player.getChatSession();

        Abilities src = player.getAbilities();
        Abilities abCopy = new Abilities();
        abCopy.invulnerable = src.invulnerable;
        abCopy.flying = src.flying;
        abCopy.mayfly = src.mayfly;
        abCopy.instabuild = src.instabuild;
        abCopy.setWalkingSpeed(src.getWalkingSpeed());
        abCopy.setFlyingSpeed(src.getFlyingSpeed());
        b.abilities = abCopy;

        b.packedAttributes = player.getAttributes().pack();

        b.gameProfile = player.getGameProfile();
        b.level = player.level();
        b.x = player.getX();
        b.y = player.getY();
        b.z = player.getZ();
        b.yRot = player.getYRot();
        b.xRot = player.getXRot();

        b.mainInventory = copyItemsFrom(player.getInventory().getNonEquipmentItems());
        b.armorInventory = copyItemsFrom(
                List.of(
                        player.getItemBySlot(EquipmentSlot.HEAD),
                        player.getItemBySlot(EquipmentSlot.CHEST),
                        player.getItemBySlot(EquipmentSlot.LEGS),
                        player.getItemBySlot(EquipmentSlot.FEET)
                )
        );
        b.offhandInventory = copyItemsFrom(List.of(player.getItemBySlot(EquipmentSlot.OFFHAND)));
        b.selectedSlot = player.getInventory().getSelectedSlot();
        b.enderChest = new ArrayList<>();
        var ec = player.getEnderChestInventory();
        for (int i = 0; i < ec.getContainerSize(); i++) {
            b.enderChest.add(ec.getItem(i).copy());
        }

        b.health = player.getHealth();
        FoodData food = player.getFoodData();
        b.foodLevel = food.getFoodLevel();
        b.saturationLevel = food.getSaturationLevel();
        b.exhaustionLevel = food.exhaustionLevel;
        b.experienceLevel = player.experienceLevel;
        b.experienceProgress = player.experienceProgress;
        b.totalExperience = player.totalExperience;
        b.gameMode = player.gameMode.getGameModeForPlayer();
        b.effects = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            b.effects.add(new MobEffectInstance(effect));
        }

        Entity v = player.getVehicle();
        if (v != null) b.vehicle = v.getUUID();
        b.passengers = player.getPassengers().stream().map(Entity::getUUID).toList();
        if (player.isSleeping() && player.getSleepingPos().isPresent()) b.isAsleep = player.getSleepingPos().get();

        List<SynchedEntityData.DataValue<?>> raw = player.getEntityData().getNonDefaultValues();
        b.entityData = raw != null ? List.copyOf(raw) : List.of();

        TagValueOutput out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        player.addAdditionalSaveData(out);
        b.additionalData = out.buildResult();

        b.xts$flightFuel = ((XTSFuelAccess) player).xts$getFlightFuel();

        return new SwapSnapshot(b);
    }

    public void applyTo(ServerPlayer player, GameProfile profileToApply) {
        PlayerSkinRefresher.applyProfileAndRefresh(player, profileToApply);
        applyInventory(player);
        applyEnderChest(player);
        player.setHealth(health);
        FoodData food = player.getFoodData();
        food.setFoodLevel(foodLevel);
        food.setSaturation(saturationLevel);
        food.exhaustionLevel = exhaustionLevel;
        player.experienceLevel = experienceLevel;
        player.setExperienceLevels(experienceLevel);
        player.totalExperience = totalExperience;
        player.setGameMode(gameMode);
        player.removeAllEffects();
        for (MobEffectInstance effect : effects) {
            player.addEffect(new MobEffectInstance(effect));
        }
        player.getInventory().setSelectedSlot(selectedSlot);
        player.teleportTo(level, x, y, z, Set.of(), yRot, xRot, false);

//        player.getAttributes().load(attributes);

        player.getAttributes().apply(attributes);

        if (!entityData.isEmpty()) {
            player.getEntityData().assignValues(entityData);
        }

        if (additionalData != null) {
            TagValueInput in = (TagValueInput) TagValueInput.create(
                    ProblemReporter.DISCARDING,
                    player.registryAccess(),
                    additionalData.copy()
            );
            player.readAdditionalSaveData(in);
        }

        applyRiding(player);
        if (isAsleep != null) {
            player.startSleepInBed(isAsleep);
        } else {
            player.clearSleepingPos();
            player.stopSleepInBed(true, true);
        }
        this.level.updateSleepingPlayerList();
        player.inventoryMenu.broadcastChanges();

        if (fallFlying) player.startFallFlying();
        else player.stopFallFlying();

        player.wonGame = wonGame;

        for (Map.Entry<Stat<?>, Integer> entry : stats.stats.object2IntEntrySet()) {
            player.getStats().setValue(player, entry.getKey(), entry.getValue());
        }
        player.getStats().sendStats(player);
        player.getStats().markAllDirty();

        player.setMainArm(mainArm);
//        player.noCulling = noCulling;

        if (chatSession != null) player.setChatSession(chatSession);

        Abilities dst = player.getAbilities();
        dst.invulnerable = abilities.invulnerable;
        dst.flying = abilities.flying;
        dst.mayfly = abilities.mayfly;
        dst.instabuild = abilities.instabuild;
        dst.setWalkingSpeed(abilities.getWalkingSpeed());
        dst.setFlyingSpeed(abilities.getFlyingSpeed());

        ((XTSFuelAccess) player).xts$setFlightFuel(xts$flightFuel);

        player.onUpdateAbilities();
    }

    private void applyInventory(ServerPlayer player) {
        copyItemsInto(player.getInventory().getNonEquipmentItems(), mainInventory);
        {
            player.setItemSlot(EquipmentSlot.HEAD, armorInventory.getFirst());
            player.setItemSlot(EquipmentSlot.CHEST, armorInventory.get(1));
            player.setItemSlot(EquipmentSlot.LEGS, armorInventory.get(2));
            player.setItemSlot(EquipmentSlot.FEET, armorInventory.get(3));
        }
//        copyItemsInto(player.getInventory().armor, armorInventory);
        {
            player.setItemSlot(EquipmentSlot.OFFHAND, offhandInventory.getFirst());
        }
//        copyItemsInto(player.getInventory().offhand, offhandInventory);
        player.inventoryMenu.broadcastChanges();
    }

    private void applyRiding(ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        for (UUID uuid : passengers) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity != null) {
                entity.startRiding(player);
            }
        }
        if (vehicle != null) {
            Entity entity = serverLevel.getEntity(vehicle);
            if (entity != null) {
                player.startRiding(entity);
            }
        }
    }

    private void applyEnderChest(ServerPlayer player) {
        var ec = player.getEnderChestInventory();
        for (int i = 0; i < Math.min(ec.getContainerSize(), enderChest.size()); i++) {
            ec.setItem(i, enderChest.get(i).copy());
        }
    }

    private static List<ItemStack> copyItemsFrom(List<ItemStack> source) {
        List<ItemStack> copy = new ArrayList<>(source.size());
        for (ItemStack stack : source) {
            copy.add(stack.copy());
        }
        return copy;
    }

    private static void copyItemsInto(List<ItemStack> target, List<ItemStack> source) {
        for (int i = 0; i < target.size(); i++) {
            target.set(i, i < source.size() ? source.get(i).copy() : ItemStack.EMPTY);
        }
    }

    private static class Builder {
        BlockPos isAsleep;
        UUID vehicle;
        List<UUID> passengers;
        GameProfile gameProfile;
        boolean fallFlying;
        boolean wonGame;
        ServerStatsCounter stats;
        List<AttributeInstance.Packed> packedAttributes;
        HumanoidArm mainArm;
        ServerGamePacketListenerImpl connection;
        RemoteChatSession chatSession;
        Abilities abilities;
        ServerLevel level;
        double x, y, z;
        float yRot, xRot;
        List<ItemStack> mainInventory, armorInventory, offhandInventory, enderChest;
        int selectedSlot;
        float health, saturationLevel, exhaustionLevel;
        int foodLevel;
        int experienceLevel;
        float experienceProgress;
        int totalExperience;
        GameType gameMode;
        List<MobEffectInstance> effects;
        List<SynchedEntityData.DataValue<?>> entityData;
        CompoundTag additionalData;
        int xts$flightFuel;
    }
}