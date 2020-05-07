package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.LangText;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;

public class HugCommand extends EssentialCommand {

    public HugCommand() {
        super("hug");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = argument("target", player())
                .suggests(ArgumentCompletions::allPlayers)
                .executes(context -> execute(context.getSource(), getPlayer(context, "target")));

        argumentBuilder.executes(context -> execute(context.getSource(), context.getSource().getPlayer()));
        commandNode.addChild(target.build());
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) {
        KiloChat.sendMessageTo(player, LangText.get(true, "command.hug.self"));
        return 1;
    }



}