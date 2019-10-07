package org.kilocraft.essentials.craft.commands.essentials;

import org.kilocraft.essentials.api.chat.LangText;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
//import org.kilocraft.essentials.craft.player.KiloPlayer;

public class NickCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("nick")
				.requires(source -> source.hasPermissionLevel(2)).executes(context -> {
					context.getSource().sendFeedback(LangText.get(true, "command.nick.onlyoneargument"), false);
					return 1;
				}).build();

		ArgumentCommandNode<ServerCommandSource, String> argument = CommandManager
				.argument("name", StringArgumentType.string()).executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayer();
					String name = context.getArgument("name", String.class);

					//KiloPlayer.get(player).setNickname(name);

					context.getSource().sendFeedback(LangText.getFormatter(true, "command.nick.success", name), false);

					return 0;
				}).build();

		dispatcher.getRoot().addChild(literalArgumentBuilder);
		literalArgumentBuilder.addChild(argument);
	}
}
