package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.chat.ChatMessage;

import java.util.Collection;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayers;
import static net.minecraft.command.arguments.EntityArgumentType.players;
import static org.kilocraft.essentials.KiloCommands.SUCCESS;
import static org.kilocraft.essentials.chat.KiloChat.*;

public class ClearchatCommand extends EssentialCommand {
    public ClearchatCommand() {
        super("clearchat", CommandPermission.CLEARCHAT);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetsArgument = argument("targets", players())
                .suggests(ArgumentCompletions::allPlayers)
                .executes(ctx -> executeMultiple(ctx, getPlayers(ctx, "targets"), false))
                .then(literal("-silent")
                        .executes(ctx -> executeMultiple(ctx, getPlayers(ctx, "targets"), true)));

        argumentBuilder.executes(ctx -> executeAll(ctx, false));
        commandNode.addChild(literal("-silent").executes(ctx -> executeAll(ctx, true)).build());
        commandNode.addChild(targetsArgument.build());
    }

    private static int executeAll(CommandContext<ServerCommandSource> ctx, boolean silent) {
        broadCastExceptConsole(new ChatMessage(getClearString(), false));

        if (!silent)
            broadCastLangExceptConsole("command.clearchat.broadcast", ctx.getSource().getName());

        broadCastToConsole(new ChatMessage(
                getFormattedLang("command.clearchat.broadcast", ctx.getSource().getName()), false));

        return SUCCESS();
    }

    private static int executeMultiple(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> targets, boolean silent) {
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
        for (int i = 0; i < 18; i++) builder.append("\n\n\n\n\n\n");
        return builder.toString();
    }

}
