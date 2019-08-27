package org.kilocraft.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class RankCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("rank")
				.then(CommandManager.literal("add").then(CommandManager.argument("name", StringArgumentType.string()))
						.then(CommandManager.literal("remove")
								.then(CommandManager.argument("name", StringArgumentType.string()))
								.then(CommandManager.literal("join")).then(CommandManager.literal("leave"))
								.then(CommandManager.literal("list")
										.then(CommandManager.argument("player", EntityArgumentType.players()))))));
	}

}
