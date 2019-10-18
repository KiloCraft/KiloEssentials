package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.chat.ChatMessage;
import org.kilocraft.essentials.craft.chat.KiloChat;

public class FlyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("fly")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.fly", 2))
                .executes(c -> toggle(c.getSource(), c.getSource().getPlayer()));
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArg = CommandManager.argument("player", EntityArgumentType.player())
                .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                .executes(c -> toggle(c.getSource(), EntityArgumentType.getPlayer(c, "player")));;
        RequiredArgumentBuilder<ServerCommandSource, Boolean> boolArg = CommandManager.argument("set", BoolArgumentType.bool())
                .executes(c -> execute(c.getSource(), EntityArgumentType.getPlayer(c, "player"), BoolArgumentType.getBool(c, "set")));

        selectorArg.then(boolArg);
        argumentBuilder.then(selectorArg);

        dispatcher.register(argumentBuilder);
    }

    private static int toggle(ServerCommandSource source, ServerPlayerEntity playerEntity) throws CommandSyntaxException {
        execute(source, playerEntity, !playerEntity.abilities.allowFlying);
        return 1;
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity playerEntity, boolean bool) throws CommandSyntaxException {
        playerEntity.abilities.allowFlying = bool;
        playerEntity.abilities.flying = bool;
        playerEntity.sendAbilitiesUpdate();

        String text = String.format("&eYou have set the fly ability to &b%s&e for &6%s&e.", playerEntity.abilities.allowFlying, playerEntity.getName().asString());
        TextFormat.sendToUniversalSource(source, text, false);

        if (!source.getPlayer().getName().equals(playerEntity.getName())) {
            String s = String.format("&e&a%s set the fly ability to &b%s for you!", source.getName(), playerEntity.abilities.allowFlying);
            KiloChat.sendMessageTo(playerEntity, new ChatMessage(s, true));
        }

        return 1;
    }
}
