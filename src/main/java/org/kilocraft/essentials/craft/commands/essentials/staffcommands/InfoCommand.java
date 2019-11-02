package org.kilocraft.essentials.craft.commands.essentials.staffcommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.user.User;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class InfoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> info = CommandManager.literal("info");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("player", EntityArgumentType.player());

        info.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.info.self", 2));
        target.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.info.others", 2));
        
        target.suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder));
        
        info.executes(context -> execute(context.getSource(), context.getSource().getPlayer()));
        target.executes(context -> execute(context.getSource(), EntityArgumentType.getPlayer(context, "player")));

        info.then(target);
        dispatcher.register(info);
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        User user = KiloServer.getServer().getUserManager().getUser(player.getUuid());

        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.nick", user.getNickname()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.name", player.getName().asString()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.uuid", player.getUuid()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.rtpleft", user.getRandomTeleportsLeft()));      
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.pos", player.getPos()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.particle", user.getDisplayParticleId()));
        source.getPlayer().sendMessage(LangText.getFormatter(true, "command.info.firstjoined", user.getFirstJoin()));
        
        return 1;
    }
}
