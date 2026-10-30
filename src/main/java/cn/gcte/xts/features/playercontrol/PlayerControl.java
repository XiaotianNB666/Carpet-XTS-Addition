package cn.gcte.xts.features.playercontrol;

import cn.gcte.xts.CarpetXTSAddition;
import cn.gcte.xts.XTSExtensionServer;
import cn.gcte.xts.rules.XTSRules;
import cn.gcte.xts.compat.Permission;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PlayerControl {

    private static final SwapManager swapManager = new SwapManager();

    @SuppressWarnings("unused")
    public static void registerCommand(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        dispatcher.register(literal("player").then(
                argument("player", word()).then(
                        literal("control")
                                .requires(source -> XTSRules.enabledFakePlayerControl()
                                        && Permission.hasPermission(source, XTSRules.fakePlayerControlCommandLevel()))
                                .executes(commandContext -> {
                                    try {
                                        String playerName = commandContext.getArgument("player", String.class);
                                        var player = Objects.requireNonNull(commandContext.getSource().getServer().getPlayerList().getPlayer(playerName));
                                        var result = swapManager.swap(commandContext.getSource().getPlayerOrException(), player);
                                        return result.success() ? 1 : 0;
                                    } catch (Exception e) {
                                        //send feed back
                                        commandContext.getSource().sendFailure(Component.translatableWithFallback("xts.player.control.error.swap_failed", "Swap failed: %s", e.getMessage()));
                                    }
                                    return 0;
                                })
                ).then(literal("release"))
                        .requires(source -> XTSRules.enabledFakePlayerControl()
                                && Permission.hasPermission(source, XTSRules.fakePlayerControlCommandLevel())
                                && ! CarpetXTSAddition.TempSharedConstants.releaseCommandAlwaysDisabled
                        ).executes(commandContext -> {
                            var result = swapManager.release(commandContext.getSource().getPlayerOrException(), commandContext.getSource().getServer());
                            return result.success() ? 1 : 0;
                        })
                )
        );
    }

    public static void registerObserver() {
        XTSExtensionServer.settingsManager().registerRuleObserver(
                (source, changedRule, userInput) -> {
                    if (changedRule.name().equals("fakePlayerControl")) {
                        var server = source.getServer();
                        server.getPlayerList().getPlayers().forEach(
                                p -> server.getCommands().sendCommands(p)
                        );
                        if (!XTSRules.enabledFakePlayerControl()) {
                            try {
                                var player = source.getPlayerOrException();
                                swapManager.release(player, source.getServer());
                            } catch (CommandSyntaxException e) {
                                CarpetXTSAddition.LOGGER.error("Failed to handle fakePlayerControls for player {} when disabling fakePlayerControl rule", source.getTextName(), e);
                            }
                        }
                    }
                }
        );
    }

    public static SwapManager getSwapManager() {
        return swapManager;
    }

}
