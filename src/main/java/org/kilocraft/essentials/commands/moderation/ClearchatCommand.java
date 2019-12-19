package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.ChatMessage;

import java.util.Collection;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayers;
import static net.minecraft.command.arguments.EntityArgumentType.players;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.SUCCESS;
import static org.kilocraft.essentials.KiloCommands.hasPermission;
import static org.kilocraft.essentials.chat.KiloChat.*;

public class ClearchatCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> clearchatCommand = dispatcher.register(literal("cc")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.CLEARCHAT))
                .executes(ctx -> executeAll(ctx, false))
                .then(literal("-silent")
                        .executes(ctx -> executeAll(ctx, true))
                )
                .then(argument("targets", players())
                        .suggests(TabCompletions::allPlayers)
                        .executes(ctx -> executeMultiple(ctx, getPlayers(ctx, "targets"), false))
                        .then(literal("-silent")
                                .executes(ctx -> executeMultiple(ctx, getPlayers(ctx, "targets"), true))
                        )
                )
        );

        dispatcher.getRoot().addChild(clearchatCommand);
        dispatcher.register(literal("clearchat").requires(src -> hasPermission(src, "clearchat", 3)).executes(ctx -> executeAll(ctx, true)).redirect(clearchatCommand));
    }

    private static int executeAll(CommandContext<ServerCommandSource> ctx, boolean silent) throws CommandSyntaxException {
        broadCastExceptConsole(new ChatMessage(getClearString(), false));

        if (!silent)
            broadCastLangExceptConsole("command.clearchat.broadcast", ctx.getSource().getName());

        broadCastToConsole(new ChatMessage(
                getFormattedLang("command.clearchat.broadcast", ctx.getSource().getName()), false));

        return SUCCESS();
    }

    private static int executeMultiple(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> targets, boolean silent) throws CommandSyntaxException {
        for (ServerPlayerEntity target : targets) {
            target.sendMessage(new LiteralText(getClearString()));

            if (!silent)
                sendLangMessageTo(target, "command.clearchat.singleton", ctx.getSource().getName());
        }

        broadCastLangToConsole("command.clearchat.singleton.broadcast", ctx.getSource().getName(), targets.size());

        return SUCCESS();
    }

    private static String getClearString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 18; i++) {
            builder.append("\n\n\n\n\n\n");
        }

        return builder.toString();
    }

}
