package org.kilocraft.essentials.commands;

import org.kilocraft.essentials.utils.LangText;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class RankCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("rank").executes(context -> {
			context.getSource().sendFeedback(LangText.get(true, "command.rank.onlyoneargument"), false);
			return 1;
		}).then(CommandManager.literal("join").executes(context -> {
			context.getSource().sendFeedback(LangText.get(true, "command.rank.noplayertojoin"), false);
			return 1;
		}).then(CommandManager.argument("player", EntityArgumentType.players()).executes(context -> {
			context.getSource().sendFeedback(LangText.get(true, "command.rank.noranktojoin"), false);
			return 1;
		}).then(CommandManager.argument("name", StringArgumentType.string()).executes(context -> {
			// TODO: Join rank
			context.getSource().sendFeedback(LangText.get(true, "command.rank.joinrank"), false);
			return 0;
		})))).then(CommandManager.literal("leave").executes(context -> {
			context.getSource().sendFeedback(LangText.get(true, "command.rank.noplayertoleave"), false);
			return 1;
		}).then(CommandManager.argument("player", EntityArgumentType.players()).executes(context -> {
			context.getSource().sendFeedback(LangText.get(true, "command.rank.noranktoleave"), false);
			return 1;
		}).then(CommandManager.argument("name", StringArgumentType.string()).executes(context -> {
			// TODO: Join rank
			context.getSource().sendFeedback(LangText.get(true, "command.rank.leaverank"), false);
			return 0;
		})))).then(CommandManager.literal("list").executes(context -> {
			// TODO: List ranks
			context.getSource().sendFeedback(LangText.get(true, "command.rank.list"), false);
			return 0;
		}).then(CommandManager.argument("player", EntityArgumentType.players()).executes(context -> {
			// TODO: List player ranks
			context.getSource().sendFeedback(LangText.get(true, "command.rank.list.player"), false);
			return 0;
		}))));
	}

}
