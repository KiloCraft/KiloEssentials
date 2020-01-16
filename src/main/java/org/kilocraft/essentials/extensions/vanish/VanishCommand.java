package org.kilocraft.essentials.extensions.vanish;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class VanishCommand {
    private static Predicate<ServerCommandSource> PERMISSION_CHECK_SELF = src -> KiloCommands.hasPermission(src, CommandPermission.VANISH_SELF);
    private static Predicate<ServerCommandSource> PERMISSION_CHECK_OTHERS = src -> KiloCommands.hasPermission(src, CommandPermission.VANISH_OTHERS);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("v")
                .requires(PERMISSION_CHECK_SELF)
                .executes(VanishCommand::execute)
                .build();

        LiteralCommandNode<ServerCommandSource> listNode = literal("list").build();
        LiteralCommandNode<ServerCommandSource> othersNode = literal("others").requires(PERMISSION_CHECK_OTHERS).build();

        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArg = argument("target", player())
                .suggests(TabCompletions::allPlayers)
                .executes(ctx -> toggle(ctx.getSource(), getPlayer(ctx, "target")));

        RequiredArgumentBuilder<ServerCommandSource, Boolean> setArg = argument("set", bool())
                .executes(ctx -> executeSet(ctx.getSource(), getPlayer(ctx, "target"), getBool(ctx, "set")));

        selectorArg.then(setArg);
        othersNode.addChild(selectorArg.build());
        rootCommand.addChild(listNode);
        rootCommand.addChild(othersNode);
        dispatcher.register(literal("vanish").requires(PERMISSION_CHECK_SELF).executes(VanishCommand::execute).redirect(rootCommand));
        dispatcher.getRoot().addChild(rootCommand);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return toggle(ctx.getSource(), ctx.getSource().getPlayer());
    }

    private static int toggle(ServerCommandSource source, ServerPlayerEntity target) {
        return executeSet(source, target, !KiloServer.getServer().getOnlineUser(target).isVanished());
    }

    private static int executeSet(ServerCommandSource source, ServerPlayerEntity target, boolean set) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(target);
        user.setVanished(set);
        KiloChat.sendLangMessageTo(source, "template.#1", "Vanish", set, user.getDisplayname());

        return 1;
    }

    private static CompletableFuture<Suggestions> settingsSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(VanishSettings.getKeys(), builder);
    }

}
