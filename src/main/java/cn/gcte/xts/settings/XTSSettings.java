package cn.gcte.xts.settings;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;

public class XTSSettings {

    public static final String XTS = "XTS";

    @Rule(
            categories = {RuleCategory.SURVIVAL, XTS}
    )
    public static boolean creativeFlight = false;

    @Rule(
            categories = {RuleCategory.SURVIVAL, XTS}
    )
    public static int creativeFlightFuelBase = 10;

    @Rule(
            categories = {RuleCategory.SURVIVAL, XTS}
    )
    public static boolean creativeFlightFallHasDamage = true;

    @Rule(
            categories = {RuleCategory.EXPERIMENTAL, XTS},
            options = {"0", "1", "2", "3", "4", "ops", "false"}
    )
    public static String fakePlayerControl = "false";
}
