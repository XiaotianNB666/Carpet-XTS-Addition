package cn.gcte.xts;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.api.settings.SettingsManager;
import carpet.utils.Translations;
import cn.gcte.xts.rules.XTSRuleRegistrar;

import java.util.Map;

public class XTSExtensionServer implements CarpetExtension {
    public static final String MOD_ID = CarpetXTSAddition.MOD_ID;
    private static final XTSExtensionServer INSTANCE = new XTSExtensionServer();

    private XTSExtensionServer() {

    }

    public static void register() {
        CarpetServer.manageExtension(INSTANCE);
    }

    public static SettingsManager settingsManager() {
        return CarpetServer.settingsManager;
    }

    @Override
    public String version() {
        return MOD_ID;
    }

    @Override
    public void onGameStarted() {
        XTSRuleRegistrar.register();
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return Translations.getTranslationFromResourcePath("assets/carpet-xts-addition/lang/" + lang + ".json");
    }
}
