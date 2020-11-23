package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;

import java.util.Collection;

import static net.minecraft.command.argument.EntityArgumentType.getPlayers;
import static net.minecraft.command.argument.EntityArgumentType.players;
import static org.kilocraft.essentials.chat.KiloChat.*;

public class ClearChatCommand extends EssentialCommand {
    public ClearChatCommand() {
        super("clearchat", CommandPermission.CLEARCHAT);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetsArgument = argument("targets", players())
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> executeMultiple(ctx, getPlayers(ctx, "targets"), false))
                .then(literal("-silent")
                        .executes(ctx -> executeMultiple(ctx, getPlayers(ctx, "targets"), true)));

        argumentBuilder.executes(ctx -> executeAll(ctx, false));
        commandNode.addChild(literal("-silent").executes(ctx -> executeAll(ctx, true)).build());
        commandNode.addChild(targetsArgument.build());
    }

    private int executeAll(CommandContext<ServerCommandSource> ctx, boolean silent) {
        broadCast(getClearString());

        if (!silent) broadCast(getFormattedLang("command.clearchat.broadcast", ctx.getSource().getName()));

        broadCastToConsole(getFormattedLang("command.clearchat.broadcast", ctx.getSource().getName()));

        return SUCCESS;
    }

    private int executeMultiple(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> targets, boolean silent) {
        for (ServerPlayerEntity target : targets) {
            getOnlineUser(target).sendMessage(new LiteralText(getClearString()));

            getOnlineUser(target).sendLangMessage("command.clearchat.singleton", ctx.getSource().getName());
        }

        broadCastToConsole(getFormattedLang("command.clearchat.singleton.broadcast", ctx.getSource().getName(), targets.size()));

        return SUCCESS;
    }

    private static String getClearString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 100; i++) builder.append("\n");
        return builder.toString();
    }

}
