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
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentCompletions;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;

public class SudoCommand extends EssentialCommand {

    public SudoCommand() {
        super("sudo", CommandPermission.SUDO_OTHERS, 4);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        ArgumentCommandNode<ServerCommandSource, EntitySelector> selectorArg = argument("target", player())
                .suggests(ArgumentCompletions::allPlayers)
                .redirect(dispatcher.getRoot(), redirectModifier())
                .build();

        LiteralCommandNode<ServerCommandSource> consoleArg = literal("-server")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.SUDO_SERVER, 4))
                .redirect(dispatcher.getRoot(), serverRedirectModifier())
                .build();

        commandNode.addChild(consoleArg);
        commandNode.addChild(selectorArg);
    }

    private static SingleRedirectModifier<ServerCommandSource> redirectModifier() {
        return context -> getPlayer(context, "target").getCommandSource();
    }

    private static SingleRedirectModifier<ServerCommandSource> serverRedirectModifier() {
        return context -> KiloServer.getServer().getVanillaServer().getCommandSource();
    }

}
