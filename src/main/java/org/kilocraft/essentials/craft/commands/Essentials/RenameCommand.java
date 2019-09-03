package org.kilocraft.essentials.craft.commands.Essentials;

import org.kilocraft.essentials.craft.utils.ChatColor;
import org.kilocraft.essentials.craft.utils.LangText;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class RenameCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("rename")
				.requires(source -> source.hasPermissionLevel(2)).executes(context -> {
					context.getSource().sendFeedback(LangText.get(true, "command.rename.onlyoneargument"), false);
					return 1;
				}).build();

		ArgumentCommandNode<ServerCommandSource, String> argument = CommandManager
				.argument("name", StringArgumentType.string()).executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayer();

					ItemStack item = player.getMainHandStack();
					if (item == null) {
						context.getSource().sendFeedback(LangText.get(true, "command.rename.noitem"), false);
						return 1;
					} else {
						if (player.experienceLevel < 1) {
							context.getSource().sendFeedback(LangText.get(true, "command.rename.noxp"), false);
							return 1;
						}

						player.experienceLevel -= 1;
						String name = context.getArgument("name", String.class);
						name = ChatColor.translateAlternateColorCodes('&', name);
						item.setCustomName(new LiteralText(name));
						context.getSource().sendFeedback(LangText.getFormatter(true, "command.rename.success", name),
								false);

						return 0;
					}
				}).build();

		dispatcher.getRoot().addChild(literalArgumentBuilder);
		literalArgumentBuilder.addChild(argument);
	}
}
