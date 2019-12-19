package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.User;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class InfoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("info.self");
        KiloCommands.getCommandPermission("info.others");
        LiteralArgumentBuilder<ServerCommandSource> info = literal("info");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = argument("player", player());

        info.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.info.self", 2));
        target.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.info.others", 2));

        target.suggests(TabCompletions::allPlayers);

        info.executes(context -> execute(context.getSource(), context.getSource().getPlayer()));
        target.executes(context -> execute(context.getSource(), getPlayer(context, "player")));

        info.then(target);
        dispatcher.register(info);
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        User user = KiloServer.getServer().getUserManager().getOnline(player.getUuid());

        if (user.hasNickname()) {
        	source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.nick", user.getNickname().get()));
        }
        
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.name", player.getName().asString()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.uuid", player.getUuid()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.rtpleft", user.getRTPsLeft()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.pos", player.getPos()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.particle", user.getDisplayParticleId()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.firstjoined", user.getFirstJoin()));
        return 1;
    }
}
