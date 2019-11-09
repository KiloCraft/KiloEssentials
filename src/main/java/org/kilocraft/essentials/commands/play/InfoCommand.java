package org.kilocraft.essentials.commands.play;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.commands.CommandSuggestions;
import org.kilocraft.essentials.user.ServerUser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class InfoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> info = literal("info");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = argument("player", player());

        info.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.info.self", 2));
        target.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.info.others", 2));
        
        target.suggests(CommandSuggestions::allPlayers);
        
        info.executes(context -> execute(context.getSource(), context.getSource().getPlayer()));
        target.executes(context -> execute(context.getSource(), getPlayer(context, "player")));

        info.then(target);
        dispatcher.register(info);
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        ServerUser serverUser = KiloServer.getServer().getUserManager().getUser(player.getUuid());

        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.nick", serverUser.getNickname()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.name", player.getName().asString()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.uuid", player.getUuid()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.rtpleft", serverUser.getRTPsLeft()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.pos", player.getPos()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.particle", serverUser.getDisplayParticleId()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.firstjoined", serverUser.getFirstJoin()));
        
        return 1;
    }
}
