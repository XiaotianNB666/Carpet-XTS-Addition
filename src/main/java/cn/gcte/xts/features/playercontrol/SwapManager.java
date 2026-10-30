package cn.gcte.xts.features.playercontrol;

import cn.gcte.xts.CarpetXTSAddition;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

import static cn.gcte.xts.CarpetXTSAddition.TempSharedConstants.selfSwapIsRelease;

public class SwapManager {
    private final Map<UUID, UUID> activeSwaps = new HashMap<>();
    private final Map<UUID, SwapSnapshot> originalStates = new HashMap<>();

    public SwapResult swap(ServerPlayer playerA, ServerPlayer playerB) {
        UUID uidA = playerA.getUUID();
        UUID uidB = playerB.getUUID();

        if (uidA.equals(uidB)) {
            if (selfSwapIsRelease) {
                this.release(playerB, playerB.server);
                return SwapResult.ignore();
            }
            return SwapResult.fail(Component.translatableWithFallback("xts.player.control.error.self_swap", "Can't swap with yourself!"));
        }
        if (activeSwaps.containsKey(uidA)) {
            return SwapResult.fail(Component.translatableWithFallback("xts.player.control.error.already_swapped", "%s is already in a swap, please release first.", playerA.getName()));
        }
        if (activeSwaps.containsKey(uidB)) {
            return SwapResult.fail(Component.translatableWithFallback("xts.player.control.error.already_swapped", "%s is already in a swap, please release first.", playerB.getName()));
        }

        SwapSnapshot snapA = SwapSnapshot.capture(playerA);
        SwapSnapshot snapB = SwapSnapshot.capture(playerB);

        activeSwaps.put(uidA, uidB);
        activeSwaps.put(uidB, uidA);
        originalStates.put(uidA, snapA);
        originalStates.put(uidB, snapB);

        snapB.applyTo(playerA, snapB.gameProfile);
        snapA.applyTo(playerB, snapA.gameProfile);

        playerA.server.getPlayerList().sendPlayerPermissionLevel(playerA);
        playerB.server.getPlayerList().sendPlayerPermissionLevel(playerB);

        return SwapResult.success(Component.empty());
    }

    public SwapResult release(ServerPlayer initiator, MinecraftServer server) {
        UUID uidA = initiator.getUUID();
        if (!activeSwaps.containsKey(uidA)) {
            return SwapResult.fail(Component.translatable("xts.player.control.error.not_swapped"));
        }

        UUID uidB = activeSwaps.get(uidA);
        ServerPlayer playerB = server.getPlayerList().getPlayer(uidB);

        SwapSnapshot origA = originalStates.get(uidA);
        SwapSnapshot origB = originalStates.get(uidB);

        SwapSnapshot currentA = SwapSnapshot.capture(initiator);
        SwapSnapshot currentB = (playerB != null) ? SwapSnapshot.capture(playerB) : null;

        activeSwaps.remove(uidA);
        activeSwaps.remove(uidB);
        originalStates.remove(uidA);
        originalStates.remove(uidB);

        if (origA == null) {
            return SwapResult.fail(Component.translatable("xts.player.control.error.original_missing"));
        }
        Objects.requireNonNullElse(currentB, origA).applyTo(initiator, origA.gameProfile);

        if (playerB != null) {
            GameProfile profileForB = (origB != null) ? origB.gameProfile : playerB.getGameProfile();
            currentA.applyTo(playerB, profileForB);
        }


        initiator.server.getPlayerList().sendPlayerPermissionLevel(initiator);
        if (playerB != null) {
            playerB.server.getPlayerList().sendPlayerPermissionLevel(playerB);
        } else {
            throw new RuntimeException("Target Player is null;");
        }

        return SwapResult.success(Component.empty());
    }

    public SwapResult forceRelease(UUID targetUid, MinecraftServer server) {
        if (!activeSwaps.containsKey(targetUid)) {
            return SwapResult.fail(Component.translatable("xts.player.control.error.not_swapped"));
        }

        ServerPlayer playerA = server.getPlayerList().getPlayer(targetUid);
        if (playerA != null) {
            return release(playerA, server);
        }

        UUID uidB = activeSwaps.get(targetUid);
        SwapSnapshot origB = originalStates.get(uidB);

        activeSwaps.remove(targetUid);
        activeSwaps.remove(uidB);
        originalStates.remove(targetUid);
        originalStates.remove(uidB);

        ServerPlayer playerB = server.getPlayerList().getPlayer(uidB);

        if (playerB != null) {
            if (origB != null) {
                origB.applyTo(playerB, origB.gameProfile);
                playerB.sendSystemMessage(Component.translatable("xts.player.control.partner_offline_restored"));
            }
            playerB.server.getPlayerList().sendPlayerPermissionLevel(playerB);
        }

        return SwapResult.success(Component.empty());
    }

    public boolean isSwapped(UUID uid) {
        return activeSwaps.containsKey(uid);
    }

    public Optional<UUID> getSwapPartner(UUID uid) {
        return Optional.ofNullable(activeSwaps.get(uid));
    }

    public List<Map.Entry<UUID, UUID>> getAllSwapPairs() {
        Set<UUID> seen = new HashSet<>();
        List<Map.Entry<UUID, UUID>> pairs = new ArrayList<>();
        for (Map.Entry<UUID, UUID> entry : activeSwaps.entrySet()) {
            if (seen.add(entry.getKey()) && seen.add(entry.getValue())) {
                pairs.add(entry);
            }
        }
        return pairs;
    }

    public record SwapResult(boolean success, boolean ignored, Component message) {
        public static SwapResult fail(Component message) {
            return new SwapResult(false, false, message);
        }

        public static SwapResult success(Component message) {
            return new SwapResult(true, false, Component.empty());
        }

        static SwapResult ignore() {
            return new SwapResult(true, true, Component.empty());
        }
    }
}
