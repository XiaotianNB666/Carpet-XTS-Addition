package cn.gcte.xts.rules;

import cn.gcte.xts.XTSExtensionServer;
import org.apache.logging.log4j.core.util.Integers;

import java.util.Objects;

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
}
