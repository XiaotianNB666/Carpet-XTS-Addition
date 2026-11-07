package cn.gcte.xts.compat;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class IdentifierCompat {
    @Contract("_, _ -> new")
    public static @NotNull Identifier of(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}
