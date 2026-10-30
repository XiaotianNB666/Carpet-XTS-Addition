package cn.gcte.xts.features.tpcollection;

import cn.gcte.xts.compat.Permission;
import cn.gcte.xts.rules.XTSRules;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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

    public static void registerCommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher,
                                        CommandBuildContext commandBuildContext) {

        dispatcher.register(
                literal("tpa")
                        .requires(stack -> XTSRules.enabledTpCommandCollection()
                                && Permission.hasPermission(stack, XTSRules.tpCommandCollectionCommandLevel()))
                        .then(
                                literal("request").then(
                                        argument("player", EntityArgument.player())
                                                .executes(ctx -> TPACommand.requestTPA(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        false
                                                ))
                                )
                        )
                        .then(
                                literal("invite").then(
                                        argument("player", EntityArgument.player())
                                                .executes(ctx -> TPACommand.requestTPA(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                true
                                                        ))
                                )
                        )
                        .then(
                                literal("accept")
                                        .executes(ctx -> TPACommand.acceptAll(ctx.getSource()))
                                        .then(
                                                argument("player", EntityArgument.player())
                                                        .suggests(TPACommand::suggestPendingRequests)
                                                        .executes(ctx -> TPACommand.acceptTPA(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "player")
                                                        ))
                                        )
                        )
                        .then(
                                literal("deny")
                                        .executes(ctx -> TPACommand.denyTPA(ctx.getSource(), null))
                                        .then(
                                                argument("player", EntityArgument.player())
                                                        .suggests(TPACommand::suggestPendingRequests)
                                                        .executes(ctx -> TPACommand.denyTPA(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "player")
                                                        ))
                                        )
                        )
                        .then(
                                literal("cancel")
                                        .executes(ctx -> TPACommand.cancelTPA(ctx.getSource()))
                        )
        );
    }

    public static final class TPACommand {

        public static int acceptAll(CommandSourceStack source) {
            ServerPlayer self = source.getPlayer();
            if (self == null) return 0;

            var requests = TPAManager.manager.listRequests(self);
            if (requests.isEmpty()) {
                self.sendSystemMessage(Component.translatableWithFallback("xts.tpa.no_request","You have no pending TPA request"));
                return 0;
            }

            int count = 0;
            for (ServerPlayer sender : requests) {
                TPAManager.manager.accept(self);
                count++;
                break;
            }
            return count;
        }

        /* ================= accept specific ================= */

        public static int acceptTPA(CommandSourceStack source, ServerPlayer target) {
            if (target == null) return acceptAll(source);
            return TPAManager.manager.accept(target) ? 1 : 0;
        }

        /* ================= deny ================= */

        public static int denyTPA(CommandSourceStack source, ServerPlayer specific) {
            ServerPlayer self = source.getPlayer();
            if (self == null) return 0;

            if (specific == null) {
                var requests = TPAManager.manager.listRequests(self);
                for (ServerPlayer sender : requests) {
                    TPAManager.manager.deny(self);
                    break;
                }
                return 1;
            } else {
                return TPAManager.manager.deny(self) ? 1 : 0;
            }
        }

        /* ================= cancel ================= */

        public static int cancelTPA(CommandSourceStack source) {
            ServerPlayer self = source.getPlayer();
            if (self == null) return 0;
            return TPAManager.manager.cancel(self) ? 1 : 0;
        }

        /* ================= request / invite ================= */

        public static int requestTPA(CommandSourceStack source, ServerPlayer target, boolean here) {
            ServerPlayer self = source.getPlayer();
            if (self == null) return 0;

            TPAManager.TPAType type = here
                    ? TPAManager.TPAType.TPA_HERE
                    : TPAManager.TPAType.TPA;

            return TPAManager.manager.send(self, target, type) ? 1 : 0;
        }


        public static CompletableFuture<Suggestions> suggestPendingRequests(
                CommandContext<CommandSourceStack> ctx,
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
    }
}