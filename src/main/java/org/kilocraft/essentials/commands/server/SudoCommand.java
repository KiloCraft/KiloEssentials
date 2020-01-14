package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.SingleRedirectModifier;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.TabCompletions;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SudoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("sudo")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.SUDO_OTHERS, 4))
                .build();

        ArgumentCommandNode<ServerCommandSource, EntitySelector> selectorArg = argument("target", player())
                .suggests(TabCompletions::allPlayers)
                .redirect(dispatcher.getRoot(), redirectModifier())
                .build();

        LiteralCommandNode<ServerCommandSource> consoleArg = literal("-server")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.SUDO_CONSOLE, 4))
                .redirect(dispatcher.getRoot(), serverRedirectModifier())
                .build();

        rootCommand.addChild(consoleArg);
        rootCommand.addChild(selectorArg);
        dispatcher.getRoot().addChild(rootCommand);
    }

    private static SingleRedirectModifier<ServerCommandSource> redirectModifier() {
        return context -> getPlayer(context, "target").getCommandSource();
    }

    private static SingleRedirectModifier<ServerCommandSource> serverRedirectModifier() {
        return context -> KiloServer.getServer().getVanillaServer().getCommandSource();
    }

}
