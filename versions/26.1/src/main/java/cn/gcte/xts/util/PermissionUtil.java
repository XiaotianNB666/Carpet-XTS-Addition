package cn.gcte.xts.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class PermissionUtil {
    public static boolean hasPermission(CommandSourceStack source, int permissionLevel) {
        return source.permissions().hasPermission(
                new Permission.HasCommandLevel(PermissionLevel.byId(permissionLevel))
        );
    }
}
