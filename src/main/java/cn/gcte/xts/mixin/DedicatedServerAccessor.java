package cn.gcte.xts.mixin;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DedicatedServer.class)
public interface DedicatedServerAccessor {
    @Accessor
    DedicatedServerSettings getSettings();

    @Accessor
    QueryThreadGs4 getQueryThreadGs4();

    @Accessor
    void setQueryThreadGs4(QueryThreadGs4 queryThreadGs4);

    @Accessor
    RconThread getRconThread();

    @Accessor
    void setRconThread(RconThread rconThread);

}
