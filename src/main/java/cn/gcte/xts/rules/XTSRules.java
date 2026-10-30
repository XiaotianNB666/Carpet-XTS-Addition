package cn.gcte.xts.rules;

import cn.gcte.xts.XTSExtensionServer;
import cn.gcte.xts.compat.Permission;

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
}
