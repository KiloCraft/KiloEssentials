package org.kilocraft.essentials.craft.commands.servermanagement;

import org.kilocraft.essentials.api.chat.LangText;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class MotdCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> motdNode = CommandManager.literal("motd").executes((context) -> {
			context.getSource().sendFeedback(LangText.get(true, "command.motd.nomotd"), false);
			return 1;
		}).build();

		LiteralCommandNode<ServerCommandSource> textNode = CommandManager.literal("text").executes((context) -> {
			String text = context.getArgument("text", String.class);
			context.getSource().sendFeedback(LangText.getFormatter(true, "command.motd.success", text), false);
			context.getSource().getMinecraftServer().setMotd(text);
			return 0;
		}).build();

		dispatcher.getRoot().addChild(motdNode);
		motdNode.addChild(textNode);
	}
}