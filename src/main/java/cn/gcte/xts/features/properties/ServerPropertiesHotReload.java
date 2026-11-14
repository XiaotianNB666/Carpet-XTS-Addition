package cn.gcte.xts.features.properties;

import cn.gcte.xts.CarpetXTSAddition;
import cn.gcte.xts.mixin.DedicatedServerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class ServerPropertiesHotReload {
    public interface Access {
        void xts$setProperties(DedicatedServerProperties newProperties);

        Path xts$getSource();
    }

    public static void reload(MinecraftServer mayDedicatedServer) {
        if (!(mayDedicatedServer instanceof DedicatedServer server)) {
            return;
        }
        var settings = (((DedicatedServerAccessor) server).getSettings());
        DedicatedServerProperties newProperties = DedicatedServerProperties.fromFile(
                ((Access) settings).xts$getSource()
        );

        ((Access) settings).xts$setProperties(newProperties);
        applyToServer(server, newProperties);
    }

    public static void applyToServer(@NotNull DedicatedServer server, @NotNull DedicatedServerProperties properties) {
        CarpetXTSAddition.LOGGER.info("[Server Properties HotReload] applying properties to server.");
        server.setPreventProxyConnections(properties.preventProxyConnections);

        var accessor = (DedicatedServerAccessor) server;
        {
            if (properties.enableQuery) {
                if (accessor.getQueryThreadGs4() == null) {
                    try {
                        CarpetXTSAddition.LOGGER.info("[Server Properties HotReload] Starting GS4 status listener");
                        accessor.setQueryThreadGs4(QueryThreadGs4.create(server));
                    } catch (Exception e) {
                        CarpetXTSAddition.LOGGER.warn("[Server Properties HotReload] failed to start QueryThread", e);
                    }
                }
            } else {
                var thread = accessor.getQueryThreadGs4();
                if (thread != null) {
                    CarpetXTSAddition.LOGGER.info("[Server Properties HotReload] Stopping GS4 status listener");
                    thread.stop();
                }
            }
        }

        {
            if (properties.enableRcon) {
                if (accessor.getRconThread() == null) {
                    try {
                        CarpetXTSAddition.LOGGER.info("[Server Properties HotReload] Starting remote control listener");
                        accessor.setRconThread(RconThread.create(server));
                    } catch (Exception e) {
                        CarpetXTSAddition.LOGGER.warn("[Server Properties HotReload] failed to start RconThread", e);
                    }
                }
            } else {
                var thread = accessor.getRconThread();
                if (thread != null) {
                    CarpetXTSAddition.LOGGER.info("[Server Properties HotReload] Stopping remote control listener");
                    thread.stop();
                }
            }
        }

        {
            //#if MC < 12111
            server.getWorldData().getGameRules().getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(Boolean.TRUE.equals(properties.announcePlayerAchievements), server);
            //#elseif MC < 260100
            //$$ server.getWorldData().getGameRules().set(GameRules.SHOW_ADVANCEMENT_MESSAGES, Boolean.TRUE.equals(properties.announcePlayerAchievements), server);
            //#else
            //$$ server.getGameRules().set(GameRules.SHOW_ADVANCEMENT_MESSAGES, Boolean.TRUE.equals(properties.announcePlayerAchievements), server);
            //#endif
        }
        server.getPlayerList().setViewDistance(server.viewDistance());
        server.getPlayerList().setSimulationDistance(server.viewDistance());
    }
}
