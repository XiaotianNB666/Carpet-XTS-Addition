package cn.gcte.xts.mixin;

import cn.gcte.xts.features.properties.ServerPropertiesHotReload;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.file.Path;

@Mixin(DedicatedServerSettings.class)
public abstract class DedicatedServerSettingsMixin implements ServerPropertiesHotReload.Access {

    @Shadow
    private DedicatedServerProperties properties;

    @Shadow
    @Final
    private Path source;

    @Unique
    public void xts$setProperties(DedicatedServerProperties newProperties) {
        this.properties = newProperties;
    }

    @Unique
    public Path xts$getSource() {
        return source;
    }

}
