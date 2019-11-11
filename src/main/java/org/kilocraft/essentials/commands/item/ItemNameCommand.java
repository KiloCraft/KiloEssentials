package org.kilocraft.essentials.commands.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextFormat;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ItemNameCommand {
	public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder) {
		LiteralArgumentBuilder<ServerCommandSource> builder = literal("name");
		LiteralArgumentBuilder<ServerCommandSource> resetArgument = literal("reset");
		LiteralArgumentBuilder<ServerCommandSource> setArgument = literal("set");
		RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name...", greedyString());

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
							getString(context, "name..."))));
				} else {
					item.setCustomName(new LiteralText(TextFormat.removeAlternateColorCodes('&',
							getString(context, "name..."))));
				}

				player.sendMessage(LangText.getFormatter(true, "command.item.name.success",
						getString(context, "name...")));
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
