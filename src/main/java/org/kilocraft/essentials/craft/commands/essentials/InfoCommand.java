package org.kilocraft.essentials.craft.commands.essentials;
import org.kilocraft.essentials.api.chat.LangText;
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
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("target",
                EntityArgumentType.player());

        info.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.info.self", 2));
        whois.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.info.self", 2));
        target.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.info.others", 2));

        info.executes(context -> {
            info(context.getSource().getPlayer());
            return 0;
        });

        whois.executes(context -> {
            info(context.getSource().getPlayer());
            return 0;
        });

        target.executes(context -> {
            info(EntityArgumentType.getPlayer(context, "target"));
            return 0;
        });

        info.then(target);
        whois.then(target);
        dispatcher.register(info);
        dispatcher.register(whois);
    }

    private static void info(ServerPlayerEntity player) {
        KiloPlayer kiloPlayer = KiloPlayerManager.getPlayerData(player.getUuid());
        player.sendMessage(LangText.getFormatter(true, "command.info.nick", kiloPlayer.nick));
        player.sendMessage(LangText.getFormatter(true, "command.info.name", player.getName().toString()));
        player.sendMessage(LangText.getFormatter(true, "command.info.rtpleft", kiloPlayer.rtpLeft));
        player.sendMessage(LangText.getFormatter(true, "command.info.uuid", player.getUuid()));
        player.sendMessage(LangText.getFormatter(true, "command.info.pos", player.getPos()));
    }
}