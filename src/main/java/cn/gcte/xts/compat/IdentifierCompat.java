package cn.gcte.xts.compat;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class IdentifierCompat {
    @Contract("_, _ -> new")
    public static @NotNull ResourceLocation of(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
