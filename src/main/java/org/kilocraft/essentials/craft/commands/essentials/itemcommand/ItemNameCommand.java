package org.kilocraft.essentials.craft.commands.essentials.itemcommand;

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
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.chat.LangText;

public class ItemNameCommand {
	public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder) {
		LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("name");
		LiteralArgumentBuilder<ServerCommandSource> resetArgument = CommandManager.literal("reset");
		LiteralArgumentBuilder<ServerCommandSource> setArgument = CommandManager.literal("set");
		RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = CommandManager.argument("name...",
				StringArgumentType.greedyString());

		nameArgument.executes(context -> {
			PlayerEntity player = context.getSource().getPlayer();
			ItemStack item = player.getMainHandStack();

			if (item == null || item.isEmpty() == true) {
				context.getSource().sendFeedback(LangText.get(true, "command.item.name.noitem"), false);
			} else {
				if (player.experienceLevel < 1 && !player.isCreative()) {
					context.getSource().sendFeedback(LangText.get(true, "command.item.name.noxp"), false);
					return 1;
				}

				if (player.isCreative() == false) {
					player.addExperienceLevels(-1);
				}

				if (Thimble.hasPermissionOrOp(context.getSource(), "kiloessentials.command.item.name.colour", 2)) {
					item.setCustomName(new LiteralText(TextFormat.translateAlternateColorCodes('&',
							StringArgumentType.getString(context, "name..."))));
				} else {
					item.setCustomName(new LiteralText(TextFormat.removeAlternateColorCodes('&',
							StringArgumentType.getString(context, "name..."))));
				}

				player.sendMessage(LangText.getFormatter(true, "command.item.name.success",
						StringArgumentType.getString(context, "name...")));
			}

			return 0;
		});

		resetArgument.executes(context -> {
			ItemStack item = context.getSource().getPlayer().getMainHandStack();
			if (item == null || item.isEmpty() == true) {
				context.getSource().sendFeedback(LangText.get(true, "command.item.name.noitem"), false);
			} else {
				context.getSource().sendFeedback(LangText.get(true, "command.item.name.reset.success"), false);
				item.setCustomName(new TranslatableText(item.getItem().getTranslationKey()));
			}

			return 0;
		});

		builder.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.item.name", 2));

		setArgument.then(nameArgument);
		builder.then(setArgument);
		builder.then(resetArgument);
		argumentBuilder.then(builder);
	}
}
