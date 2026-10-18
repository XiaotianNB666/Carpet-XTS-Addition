package cn.gcte.xts.rules;

import carpet.CarpetServer;
import cn.gcte.xts.settings.XTSSettings;

public class XTSRuleRegistrar {
    public static void register() {
        CarpetServer.settingsManager.parseSettingsClass(XTSSettings.class);
    }
}
