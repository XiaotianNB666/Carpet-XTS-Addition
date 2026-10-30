package cn.gcte.xts.compat;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.PermissionLevel;
import org.apache.logging.log4j.core.util.Integers;

import java.util.Objects;

@SuppressWarnings("unused")
public class Permission {
    public static int parsePermission(String value) {

        if (Objects.equals(value, "ops")) {
            return 1;
        } else {
            try {
                return Math.clamp(Integers.parseInt(value), 0, 4);
            } catch (NumberFormatException e) {
                return 4;
            }
        }
    }

    public static boolean hasPermission(CommandSourceStack source, int permissionLevel) {
        return source.permissions().hasPermission(
                new net.minecraft.server.permissions.Permission.HasCommandLevel(PermissionLevel.byId(permissionLevel))
        );
    }
}
