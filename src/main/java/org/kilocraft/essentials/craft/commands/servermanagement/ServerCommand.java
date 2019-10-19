package org.kilocraft.essentials.craft.commands.servermanagement;

import org.kilocraft.essentials.api.chat.LangText;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.KiloCommands;

public class ServerCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		KiloCommands.getCommandPermission("server");
		LiteralCommandNode<ServerCommandSource> serverNode = CommandManager.literal("server").requires(s -> Thimble.hasPermissionChildOrOp(s, KiloCommands.getCommandPermission("server"), 2)).executes((context) -> {
			context.getSource().sendFeedback(LangText.get(true, "command.server.nosubcommand"), false);
			return 1;
		}).build();
		
		LiteralCommandNode<ServerCommandSource> motdNode = CommandManager.literal("motd").executes((context) -> {
			context.getSource().sendFeedback(LangText.get(true, "command.motd.nomotd"), false);
			return 1;
		}).build();

		ArgumentCommandNode<ServerCommandSource, String> textNode = CommandManager
				.argument("text", StringArgumentType.greedyString()).executes((context) -> {
					String text = context.getArgument("text", String.class);
					context.getSource().sendFeedback(LangText.getFormatter(true, "command.motd.success", text), false);
					//KiloEssentials.motd = text;

					return 0;
				}).build();

		dispatcher.getRoot().addChild(serverNode);
		serverNode.addChild(motdNode);
		motdNode.addChild(textNode);
	}
}