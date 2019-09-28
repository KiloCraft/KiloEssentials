package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class ColoursCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("colours").executes(context -> {
			return execute(context);
		}));

		dispatcher.register(CommandManager.literal("colors").executes(context -> {
			return execute(context);
		}));
	}

	public static int execute(CommandContext<ServerCommandSource> context) {
		LiteralText text = new LiteralText(
				"");
		context.getSource().sendFeedback(text, false);
		return 0;
	}
}
