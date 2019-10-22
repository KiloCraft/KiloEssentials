package org.kilocraft.essentials.craft.commands.essentials;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.player.KiloPlayer;
import org.kilocraft.essentials.craft.player.KiloPlayerManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class InfoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> info = CommandManager.literal("info");
        LiteralArgumentBuilder<ServerCommandSource> whois = CommandManager.literal("whois");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("player", EntityArgumentType.player());

        info.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.info.self", 2));
        whois.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.info.self", 2));
        target.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.info.others", 2));

        target.suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder));

        info.executes(context -> execute(context.getSource(), context.getSource().getPlayer()));

        whois.executes(context -> execute(context.getSource(), context.getSource().getPlayer()));

        target.executes(context -> execute(context.getSource(), EntityArgumentType.getPlayer(context, "player")));

        info.then(target);
        whois.then(target);
        dispatcher.register(info);
        dispatcher.register(whois);
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) {
        KiloPlayer kiloPlayer = KiloPlayerManager.getPlayerData(player.getUuid());

        TextFormat.sendToUniversalSource(source, LangText.getFormatter(true, "command.info.nick", kiloPlayer.nick), false);
        TextFormat.sendToUniversalSource(source, LangText.getFormatter(true, "command.info.name", player.getName().toString()), false);
        TextFormat.sendToUniversalSource(source, LangText.getFormatter(true, "command.info.rtpleft", kiloPlayer.rtpLeft), false);
        TextFormat.sendToUniversalSource(source, LangText.getFormatter(true, "command.info.uuid", player.getUuid()), false);
        TextFormat.sendToUniversalSource(source, LangText.getFormatter(true, "command.info.pos", player.getPos()), false);

        return 1;
    }
}
