package cn.gcte.xts;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.api.settings.SettingsManager;
import carpet.utils.Translations;
import cn.gcte.xts.features.playercontrol.PlayerControl;
import cn.gcte.xts.features.tpcollection.HomeManager;
import cn.gcte.xts.features.tpcollection.TPCollection;
import cn.gcte.xts.rules.XTSRuleRegistrar;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

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

        PlayerControl.registerObserver();
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return Translations.getTranslationFromResourcePath("assets/carpet-xts-addition/lang/" + lang + ".json");
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext commandBuildContext) {
        PlayerControl.registerCommand(dispatcher, commandBuildContext);
        TPCollection.registerCommands(dispatcher, commandBuildContext);
    }

    @Override
    public void onPlayerLoggedIn(ServerPlayer player) {
        HomeManager.onPlayerJoin(player);
    }

    @Override
    public void onPlayerLoggedOut(ServerPlayer player) {
        HomeManager.onPlayerLeave(player);
    }
}
