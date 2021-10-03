package org.kilocraft.essentials.util.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.CommandPermission;

import java.util.Collection;

import static net.minecraft.command.argument.EntityArgumentType.getPlayers;
import static net.minecraft.command.argument.EntityArgumentType.players;
import static org.kilocraft.essentials.chat.KiloChat.*;

public class ClearChatCommand extends EssentialCommand {
    public ClearChatCommand() {
        super("clearchat", CommandPermission.CLEARCHAT);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetsArgument = this.argument("targets", players())
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> this.executeMultiple(ctx, getPlayers(ctx, "targets"), false))
                .then(this.literal("-silent")
                        .executes(ctx -> this.executeMultiple(ctx, getPlayers(ctx, "targets"), true)));

        this.argumentBuilder.executes(ctx -> this.executeAll(ctx, false));
        this.commandNode.addChild(this.literal("-silent").executes(ctx -> this.executeAll(ctx, true)).build());
        this.commandNode.addChild(targetsArgument.build());
    }

    private int executeAll(CommandContext<ServerCommandSource> ctx, boolean silent) {
        broadCast(getClearString());

        if (!silent) broadCast(ModConstants.translation("command.clearchat.broadcast", ctx.getSource().getName()));

        broadCastToConsole(ModConstants.translation("command.clearchat.broadcast", ctx.getSource().getName()));

        return SUCCESS;
    }

    private int executeMultiple(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> targets, boolean silent) {
        for (ServerPlayerEntity target : targets) {
            this.getOnlineUser(target).sendMessage(new LiteralText(getClearString()));

            this.getOnlineUser(target).sendLangMessage("command.clearchat.singleton", ctx.getSource().getName());
        }

        broadCastToConsole(ModConstants.translation("command.clearchat.singleton.broadcast", ctx.getSource().getName(), targets.size()));

        return SUCCESS;
    }

    private static String getClearString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 100; i++) builder.append("\n");
        return builder.toString();
    }

}
