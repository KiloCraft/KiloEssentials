package org.kilocraft.essentials.craft.commands.essentials.ItemCommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.api.chat.ChatColor;
import org.kilocraft.essentials.api.chat.LangText;

public class ItemLoreCommand {
	public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder) {
		LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("lore")
				.requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.item.lore", 3));

		LiteralArgumentBuilder<ServerCommandSource> resetArgument = CommandManager.literal("reset");
		LiteralArgumentBuilder<ServerCommandSource> setArgument = CommandManager.literal("set");
		RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = CommandManager.argument("line",
				IntegerArgumentType.integer(0, 10));
		RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = CommandManager
				.argument("name...", StringArgumentType.greedyString()).executes(context -> {
					PlayerEntity player = context.getSource().getPlayer();
					ItemStack item = player.getMainHandStack();

					if (item == null) {
						context.getSource().sendFeedback(LangText.get(true, "command.rename.noitem"), false);
					} else {
						if (player.experienceLevel < 1 && !player.isCreative()) {
							context.getSource().sendFeedback(LangText.get(true, "command.rename.noxp"), false);
						}

						if (player.isCreative() == false) {
							player.addExperienceLevels(-1);
						}

						item.setCustomName(new LiteralText(ChatColor.translateAlternateColorCodes('&',
								StringArgumentType.getString(context, "name..."))));

						player.sendMessage(LangText.getFormatter(true, "command.rename.success",
								StringArgumentType.getString(context, "name...")));
					}

					return 1;
				});
		
		builder.then(setArgument);
		builder.then(resetArgument);
		setArgument.then(nameArgument);
		nameArgument.then(lineArgument);
		argumentBuilder.then(builder);

		resetArgument.executes(context -> {
			ItemStack item = context.getSource().getPlayer().getMainHandStack();
			CompoundTag itemTag = item.getTag();

			if (item.isEmpty() == true) {
				context.getSource().sendFeedback(LangText.get(true, "command.rename.noitem"), false);
			} else {
				if (itemTag == null || !itemTag.containsKey("display")
						|| !itemTag.getCompound("display").containsKey("Lore")) {
					context.getSource().sendFeedback(LangText.get(true, "command.rename.nolore"), false);
				} else {
					itemTag.getCompound("display").remove("Lore");
					item.setCustomName(new TranslatableText(item.getItem().getTranslationKey()));
				}
			}

			return 1;
		});
	}
}
