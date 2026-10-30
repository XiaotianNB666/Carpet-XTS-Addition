package cn.gcte.xts.features.tpcollection;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TPAManager {

    public static final TPAManager manager = new TPAManager();

    private static final int EXPIRE_SECONDS = 60;

    private final Map<UUID, TPARequest> requests = new ConcurrentHashMap<>();

    private TPAManager() {}

    public enum TPAType {
        TPA,
        TPA_HERE
    }

    public record TPARequest(
            ServerPlayer sender,
            ServerPlayer target,
            TPAType type,
            long expireAt
    ) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }


    private static boolean isPlayerOffline(ServerPlayer player) {
        return player == null || player.server.getPlayerList().getPlayer(player.getUUID()) == null;
    }

    private void cleanupExpired() {
        requests.entrySet().removeIf(entry -> {
            TPARequest req = entry.getValue();
            if (req.isExpired() || isPlayerOffline(req.sender) || isPlayerOffline(req.target)) {
                if (!req.isExpired()) {
                    req.sender.sendSystemMessage(Component.translatableWithFallback("xts.tpa.expired", "Your TPA request expired (player offline)"));
                }
                return true;
            }
            return false;
        });
    }

    public boolean send(ServerPlayer sender, ServerPlayer target, TPAType type) {
        cleanupExpired();

        if (sender == target) {
            sender.sendSystemMessage(Component.translatableWithFallback("xts.tpa.self", "You cannot send a TPA request to yourself"));
            return false;
        }

        if (isPlayerOffline(target)) {
            sender.sendSystemMessage(Component.translatableWithFallback("xts.tpa.offline", "Target player is not online"));
            return false;
        }

        for (TPARequest req : requests.values()) {
            if (req.sender == sender && req.target == target) {
                sender.sendSystemMessage(Component.translatableWithFallback("xts.tpa.already_sent", "You already sent a TPA request, please wait"));
                return false;
            }
        }

        UUID id = UUID.randomUUID();
        requests.put(id, new TPARequest(
                sender,
                target,
                type,
                System.currentTimeMillis() + EXPIRE_SECONDS * 1000L
        ));

        sender.sendSystemMessage(Component.translatableWithFallback("xts.tpa.sent", "TP request sent to %s", target.getName().getString()));

        target.sendSystemMessage(
                Component.translatableWithFallback("xts.tpa.sent_to_you", "%s sent you a TPA request.", sender.getName().getString())
        );

        return true;
    }

    public boolean accept(ServerPlayer target) {
        cleanupExpired();

        Optional<Map.Entry<UUID, TPARequest>> found = requests.entrySet().stream()
                .filter(e ->
                        e.getValue().type() == TPAType.TPA? e.getValue().target == target : e.getValue().sender == target
                )
                .findFirst();

        if (found.isEmpty()) {
            target.sendSystemMessage(Component.translatableWithFallback("xts.tpa.no_request", "You have no pending TPA request"));
            return false;
        }

        TPARequest req = found.get().getValue();

        if (isPlayerOffline(req.sender)) {
            target.sendSystemMessage(Component.translatableWithFallback("xts.tpa.sender_offline", "Sender is no longer online"));
            requests.remove(found.get().getKey());
            return false;
        }

        if (req.type == TPAType.TPA) {
            req.sender.teleportTo(
                    target.level(),
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    Set.of(),
                    target.getYRot(),
                    target.getXRot(),
                    true
            );
            req.sender.sendSystemMessage(Component.translatableWithFallback("xts.tpa.accepted", "TPA accepted, you teleported to %s", target.getName().getString()));
        } else {
            req.target.teleportTo(
                    req.sender.level(),
                    req.sender.getX(),
                    req.sender.getY(),
                    req.sender.getZ(),
                    Set.of(),
                    req.sender.getYRot(),
                    req.sender.getXRot(),
                    true
            );
            target.sendSystemMessage(Component.translatableWithFallback("xts.tpa.accepted_here", "TPA accepted, %s teleported to you", req.sender.getName().getString()));
        }

        requests.remove(found.get().getKey());
        return true;
    }

    public boolean deny(ServerPlayer target) {
        cleanupExpired();

        Optional<Map.Entry<UUID, TPARequest>> found = requests.entrySet().stream()
                .filter(e -> e.getValue().target == target)
                .findFirst();

        if (found.isEmpty()) {
            target.sendSystemMessage(Component.translatableWithFallback("xts.tpa.no_request", "You have no pending TPA request"));
            return false;
        }

        TPARequest req = found.get().getValue();
        req.sender.sendSystemMessage(
                Component.translatableWithFallback("xts.tpa.denied", "%s denied your TPA request", target.getName().getString())
        );
        target.sendSystemMessage(Component.translatableWithFallback("xts.tpa.denied_notify", "Denied TPA request"));

        requests.remove(found.get().getKey());
        return true;
    }

    public boolean cancel(ServerPlayer sender) {
        cleanupExpired();

        Optional<Map.Entry<UUID, TPARequest>> found = requests.entrySet().stream()
                .filter(e -> e.getValue().sender == sender)
                .findFirst();

        if (found.isEmpty()) {
            sender.sendSystemMessage(Component.translatableWithFallback("xts.tpa.no_sent", "You have no TPA request to cancel"));
            return false;
        }

        TPARequest req = found.get().getValue();
        req.target.sendSystemMessage(
                Component.translatableWithFallback("xts.tpa.cancelled_notify", "%s cancelled their TPA request", req.sender.getName().toString())
        );

        sender.sendSystemMessage(Component.translatableWithFallback("xts.tpa.cancelled", "Cancelled TPA request"));
        requests.remove(found.get().getKey());
        return true;
    }

    public Set<ServerPlayer> listRequests(ServerPlayer target) {
        cleanupExpired();
        return requests.values().stream()
                .filter(req -> req.target == target)
                .map(req -> req.sender)
                .collect(Collectors.toSet());
    }

    public Set<ServerPlayer> listSent(ServerPlayer sender) {
        cleanupExpired();
        return requests.values().stream()
                .filter(req -> req.sender == sender)
                .map(req -> req.target)
                .collect(Collectors.toSet());
    }
}