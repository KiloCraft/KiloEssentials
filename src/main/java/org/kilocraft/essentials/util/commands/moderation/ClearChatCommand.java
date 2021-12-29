package org.kilocraft.essentials.util.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.CommandPermission;

import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.arguments.EntityArgument.getPlayers;
import static net.minecraft.commands.arguments.EntityArgument.players;
import static org.kilocraft.essentials.chat.KiloChat.*;

public class ClearChatCommand extends EssentialCommand {
    public ClearChatCommand() {
        super("clearchat", CommandPermission.CLEARCHAT);
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targetsArgument = this.argument("targets", players())
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> this.executeMultiple(ctx, getPlayers(ctx, "targets"), false))
                .then(this.literal("-silent")
                        .executes(ctx -> this.executeMultiple(ctx, getPlayers(ctx, "targets"), true)));

        this.argumentBuilder.executes(ctx -> this.executeAll(ctx, false));
        this.commandNode.addChild(this.literal("-silent").executes(ctx -> this.executeAll(ctx, true)).build());
        this.commandNode.addChild(targetsArgument.build());
    }

    private int executeAll(CommandContext<CommandSourceStack> ctx, boolean silent) {
        broadCast(getClearString());

        if (!silent) broadCast(ModConstants.translation("command.clearchat.broadcast", ctx.getSource().getTextName()));

        broadCastToConsole(ModConstants.translation("command.clearchat.broadcast", ctx.getSource().getTextName()));

        return SUCCESS;
    }

    private int executeMultiple(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, boolean silent) {
        for (ServerPlayer target : targets) {
            this.getOnlineUser(target).sendMessage(new TextComponent(getClearString()));

            this.getOnlineUser(target).sendLangMessage("command.clearchat.singleton", ctx.getSource().getTextName());
        }

        broadCastToConsole(ModConstants.translation("command.clearchat.singleton.broadcast", ctx.getSource().getTextName(), targets.size()));

        return SUCCESS;
    }

    private static String getClearString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 100; i++) builder.append("\n");
        return builder.toString();
    }

}
