package cn.gcte.xts.rules;

import cn.gcte.xts.XTSExtensionServer;
import cn.gcte.xts.compat.Permission;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class XTSRules {
    public static boolean creativeFlight() {
        return (Boolean) XTSExtensionServer.settingsManager().getCarpetRule("creativeFlight").value();
    }

    public static int creativeFlightFuelBase() {
        return (int) XTSExtensionServer.settingsManager().getCarpetRule("creativeFlightFuelBase").value();
    }

    public static boolean creativeFlightFallHasDamage() {
        return (Boolean) XTSExtensionServer.settingsManager().getCarpetRule("creativeFlightFallHasDamage").value();
    }

    public static boolean enabledFakePlayerControl() {
        String value = (String) XTSExtensionServer.settingsManager().getCarpetRule("fakePlayerControl").value();
        return !value.equalsIgnoreCase("false");
    }

    public static int fakePlayerControlCommandLevel() {
        String value = (String) XTSExtensionServer.settingsManager().getCarpetRule("fakePlayerControl").value();
        return Permission.parsePermission(value);
    }

    public static boolean enabledTpCommandCollection() {
        String value = (String) XTSExtensionServer.settingsManager().getCarpetRule("tpCommandCollection").value();
        return !value.equalsIgnoreCase("false");
    }

    public static int tpCommandCollectionCommandLevel() {
        String value = (String) XTSExtensionServer.settingsManager().getCarpetRule("tpCommandCollection").value();
        return Permission.parsePermission(value);
    }

    public static boolean betterArmorTrims() {
        return (boolean) XTSExtensionServer.settingsManager().getCarpetRule("betterArmorTrims").value();
    }

    private static boolean enabledRestartServerCommand() {
        String value = (String) XTSExtensionServer.settingsManager().getCarpetRule("restartServerCommand").value();
        return !value.equalsIgnoreCase("false");
    }

    public static boolean enabledRestartServerCommand(@NotNull MinecraftServer server) {
        return server.isDedicatedServer() && enabledRestartServerCommand();
    }

    public static int restartServerCommandLevel() {
        String value = (String) XTSExtensionServer.settingsManager().getCarpetRule("restartServerCommand").value();
        return Permission.parsePermission(value);
    }

    public static boolean hasRestartScriptPath() {
        return !restartServerScriptPath().equalsIgnoreCase("none");
    }

    public static String restartServerScriptPath() {
        return (String) XTSExtensionServer.settingsManager().getCarpetRule("restartServerScriptPath").value();
    }
}
