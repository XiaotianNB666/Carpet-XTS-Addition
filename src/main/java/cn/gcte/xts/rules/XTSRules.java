package cn.gcte.xts.rules;

import cn.gcte.xts.XTSExtensionServer;

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
}
