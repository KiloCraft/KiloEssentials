package org.kilocraft.essentials.craft.commands.essentials.ItemCommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.api.chat.ChatColor;
import org.kilocraft.essentials.api.chat.LangText;

public class ItemNameCommand {
	public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder) {
		/*Thimble.permissionWriters.add(pair -> {
//			try {
//				Thimble.PERMISSIONS.getPermission("kiloessentials.command.item.name", CommandPermission.class); // Permission that updates command tree
//			} catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
//				e.printStackTrace();
//			}
		});*/
		LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("name")/*
				.requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.item.name", 3))*/;

		LiteralArgumentBuilder<ServerCommandSource> resetArgument = CommandManager.literal("reset");
		LiteralArgumentBuilder<ServerCommandSource> setArgument = CommandManager.literal("set");
		RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = CommandManager
				.argument("name...", StringArgumentType.greedyString()).executes(context -> {
					PlayerEntity player = context.getSource().getPlayer();
					ItemStack item = player.getMainHandStack();

					if (item == null) {
						context.getSource().sendFeedback(LangText.get(true, "command.item.name.noitem"), false);
					} else {
						if (player.experienceLevel < 1 && !player.isCreative()) {
							context.getSource().sendFeedback(LangText.get(true, "command.item.name.noxp"), false);
							return 1;
						}

						if (player.isCreative() == false) {
							player.addExperienceLevels(-1);
						}

						item.setCustomName(new LiteralText(ChatColor.translateAlternateColorCodes('&',
								StringArgumentType.getString(context, "name..."))));
						
						player.sendMessage(LangText.getFormatter(true, "command.item.name.success",
								StringArgumentType.getString(context, "name...")));
					}

					return 0;
				});

		builder.then(setArgument);
		setArgument.then(nameArgument);
		builder.then(resetArgument);
		argumentBuilder.then(builder);

		resetArgument.executes(context -> {
			ItemStack item = context.getSource().getPlayer().getMainHandStack();
			if (item.isEmpty() == true) {
				context.getSource().sendFeedback(LangText.get(true, "command.item.name.noitem"), false);
			} else {
				context.getSource().sendFeedback(LangText.get(true, "command.item.name.reset.success"), false);
				item.setCustomName(new TranslatableText(item.getItem().getTranslationKey()));
			}

			return 0;
		});
	}
}
