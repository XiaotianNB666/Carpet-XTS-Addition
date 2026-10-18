package cn.gcte.xts.rules;

import cn.gcte.xts.XTSExtensionServer;

public class XTSRules {
    public static boolean creativeFlight() {
        return (Boolean) XTSExtensionServer.settingsManager().getCarpetRule("creativeFlight").value();
    }
}
