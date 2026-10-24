package cn.gcte.xts.util;

import net.minecraft.commands.CommandSourceStack;

public class PermissionUtil {
    public static boolean hasPermission(CommandSourceStack source, int permissionLevel) {
        return source.hasPermission(permissionLevel);
    }
}
