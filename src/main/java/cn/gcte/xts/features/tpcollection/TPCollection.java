package cn.gcte.xts.features.tpcollection;

import cn.gcte.xts.compat.Permission;
import cn.gcte.xts.rules.XTSRules;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TPCollection {

    public static void registerCommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        TPACommand.registerTPACommands(dispatcher, commandBuildContext);
        HomeCommand.registerHomeCommands(dispatcher, commandBuildContext);
    }

    private static final class TPACommand {

        private static int acceptAll(@NotNull CommandSourceStack source) {
            ServerPlayer self = source.getPlayer();
            if (self == null) return 0;

            var requests = TPAManager.manager.listRequests(self);
            if (requests.isEmpty()) {
                self.sendSystemMessage(Component.translatableWithFallback("xts.tpa.no_request", "You have no pending TPA request"));
                return 0;
            }

            int count = 0;
            for (ServerPlayer ignored : requests) {
                TPAManager.manager.accept(self);
                count++;
                break;
            }
            return count;
        }


        private static int acceptTPA(CommandSourceStack source, ServerPlayer target) {
            if (target == null) return acceptAll(source);
            return TPAManager.manager.accept(target) ? 1 : 0;
        }


        private static int denyTPA(@NotNull CommandSourceStack source, ServerPlayer specific) {
            ServerPlayer self = source.getPlayer();
            if (self == null) return 0;

            if (specific == null) {
                var requests = TPAManager.manager.listRequests(self);
                for (ServerPlayer ignored : requests) {
                    TPAManager.manager.deny(self);
                    break;
                }
                return 1;
            } else {
                return TPAManager.manager.deny(self) ? 1 : 0;
            }
        }


        private static int cancelTPA(@NotNull CommandSourceStack source) {
            ServerPlayer self = source.getPlayer();
            if (self == null) return 0;
            return TPAManager.manager.cancel(self) ? 1 : 0;
        }


        private static int requestTPA(@NotNull CommandSourceStack source, ServerPlayer target, boolean here) {
            ServerPlayer self = source.getPlayer();
            if (self == null) return 0;

            TPAManager.TPAType type = here
                    ? TPAManager.TPAType.TPA_HERE
                    : TPAManager.TPAType.TPA;

            return TPAManager.manager.send(self, target, type) ? 1 : 0;
        }


        private static CompletableFuture<Suggestions> suggestPendingRequests(
                @NotNull CommandContext<CommandSourceStack> ctx,
                SuggestionsBuilder builder
        ) {
            CommandSourceStack source = ctx.getSource();
            ServerPlayer self = source.getPlayer();
            if (self == null) return builder.buildFuture();

            for (ServerPlayer sender : TPAManager.manager.listRequests(self)) {
                String name = sender.getName().getString();
                if (name.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(name);
                }
            }
            return builder.buildFuture();
        }

        @SuppressWarnings("unused")
        private static void registerTPACommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
            dispatcher.register(
                    literal("tpa")
                            .requires(stack -> XTSRules.enabledTpCommandCollection()
                                    && Permission.hasPermission(stack, XTSRules.tpCommandCollectionCommandLevel()))
                            .then(
                                    literal("request").then(
                                            argument("player", EntityArgument.player())
                                                    .executes(ctx -> requestTPA(
                                                            ctx.getSource(),
                                                            EntityArgument.getPlayer(ctx, "player"),
                                                            false
                                                    ))
                                    )
                            )
                            .then(
                                    literal("invite").then(
                                            argument("player", EntityArgument.player())
                                                    .executes(ctx -> requestTPA(
                                                            ctx.getSource(),
                                                            EntityArgument.getPlayer(ctx, "player"),
                                                            true
                                                    ))
                                    )
                            )
                            .then(
                                    literal("accept")
                                            .executes(ctx -> acceptAll(ctx.getSource()))
                                            .then(
                                                    argument("player", EntityArgument.player())
                                                            .suggests(TPACommand::suggestPendingRequests)
                                                            .executes(ctx -> acceptTPA(
                                                                    ctx.getSource(),
                                                                    EntityArgument.getPlayer(ctx, "player")
                                                            ))
                                            )
                            )
                            .then(
                                    literal("deny")
                                            .executes(ctx -> denyTPA(ctx.getSource(), null))
                                            .then(
                                                    argument("player", EntityArgument.player())
                                                            .suggests(TPACommand::suggestPendingRequests)
                                                            .executes(ctx -> denyTPA(
                                                                    ctx.getSource(),
                                                                    EntityArgument.getPlayer(ctx, "player")
                                                            ))
                                            )
                            )
                            .then(
                                    literal("cancel")
                                            .executes(ctx -> cancelTPA(ctx.getSource()))
                            )
            );
        }
    }

    private static final class HomeCommand {
        @SuppressWarnings("unused")
        private static void registerHomeCommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
            dispatcher.register(literal("sethome")
                    .requires(stack -> XTSRules.enabledTpCommandCollection()
                            && Permission.hasPermission(stack, XTSRules.tpCommandCollectionCommandLevel()))
                    .executes(HomeCommand::setHome));

            dispatcher.register(literal("home")
                    .requires(stack -> XTSRules.enabledTpCommandCollection()
                            && Permission.hasPermission(stack, XTSRules.tpCommandCollectionCommandLevel()))
                    .executes(HomeCommand::home));

            dispatcher.register(literal("delhome")
                    .requires(stack -> XTSRules.enabledTpCommandCollection()
                            && Permission.hasPermission(stack, XTSRules.tpCommandCollectionCommandLevel()))
                    .executes(HomeCommand::delHome));
        }

        private static int setHome(CommandContext<CommandSourceStack> ctx) {
            var source = ctx.getSource();
            var player = source.getPlayer();
            if (player == null) {
                source.sendFailure(Component.translatableWithFallback("xts.home.player_only", "Only players can execute this command."));
                return 0;
            }
            HomeManager.setHome(player);
            source.sendSuccess(() -> Component.translatableWithFallback("xts.home.set_success", "Home set."), false);
            return Command.SINGLE_SUCCESS;
        }

        private static int home(@NotNull CommandContext<CommandSourceStack> ctx) {
            var source = ctx.getSource();
            var player = source.getPlayer();
            if (player == null) {
                source.sendFailure(Component.translatableWithFallback("xts.home.player_only", "Only players can execute this command."));
                return 0;
            }
            if (!HomeManager.hasHome(player.getUUID())) {
                source.sendFailure(Component.translatableWithFallback("xts.home.not_set", "You haven't set a home yet."));
                return 0;
            }
            boolean ok = HomeManager.teleportToHome(player);
            if (!ok) {
                source.sendFailure(Component.translatableWithFallback("xts.home.teleport_failed", "Home dimension not found or teleport failed."));
                return 0;
            }
            source.sendSuccess(() -> Component.translatableWithFallback("xts.home.teleport_success", "Teleported to home."), false);
            return Command.SINGLE_SUCCESS;
        }

        private static int delHome(@NotNull CommandContext<CommandSourceStack> ctx) {
            var source = ctx.getSource();
            var player = source.getPlayer();
            if (player == null) {
                source.sendFailure(Component.translatableWithFallback("xts.home.player_only", "Only players can execute this command."));
                return 0;
            }
            HomeManager.removeHome(player);
            source.sendSuccess(() -> Component.translatableWithFallback("xts.home.deleted", "Home deleted."), false);
            return Command.SINGLE_SUCCESS;
        }
    }
}