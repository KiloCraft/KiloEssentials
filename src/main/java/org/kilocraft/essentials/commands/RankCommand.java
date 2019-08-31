package org.kilocraft.essentials.commands;

import org.kilocraft.essentials.Mod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class RankCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("rank").executes(context -> {
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.onlyoneargument"))
					.setStyle(new Style().setColor(Formatting.RED)), false);
			return 1;
		}).then(CommandManager.literal("add").executes(context -> {
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.noranktoadd"))
					.setStyle(new Style().setColor(Formatting.RED)), false);
			return 1;
		}).then(CommandManager.argument("name", StringArgumentType.string()).executes(context -> {
			// TODO: Add rank
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.addrank")), false);
			return 0;
		}))).then(CommandManager.literal("remove").executes(context -> {
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.noranktoremove"))
					.setStyle(new Style().setColor(Formatting.RED)), false);
			return 1;
		}).then(CommandManager.argument("name", StringArgumentType.string()).executes(context -> {
			// TODO: Remove rank
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.removerank")), false);
			return 0;
		}))).then(CommandManager.literal("join").executes(context -> {
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.noplayertojoin"))
					.setStyle(new Style().setColor(Formatting.RED)), false);
			return 1;
		}).then(CommandManager.argument("player", EntityArgumentType.players()).executes(context -> {
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.noranktojoin"))
					.setStyle(new Style().setColor(Formatting.RED)), false);
			return 1;
		}).then(CommandManager.argument("name", StringArgumentType.string()).executes(context -> {
			// TODO: Join rank
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.joinrank")), false);
			return 0;
		}))).then(CommandManager.literal("leave").executes(context -> {
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.noplayertoleave"))
					.setStyle(new Style().setColor(Formatting.RED)), false);
			return 1;
		}).then(CommandManager.argument("player", EntityArgumentType.players()).executes(context -> {
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.noranktoleave"))
					.setStyle(new Style().setColor(Formatting.RED)), false);
			return 1;
		}).then(CommandManager.argument("name", StringArgumentType.string()).executes(context -> {
			// TODO: Join rank
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.joinrank")), false);
			return 0;
		})))).then(CommandManager.literal("list").executes(context -> {
			// TODO: List ranks
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.list")), false);
			return 0;
		}).then(CommandManager.argument("player", EntityArgumentType.players()).executes(context -> {
			// TODO: List player ranks
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.rank.list.player")), false);
			return 0;
		})))));
	}

}
