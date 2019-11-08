package org.kilocraft.essentials.craft.commands.essentials.itemcommand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextFormat;

public class ItemLoreCommand {
	public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder) {
		LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("lore");
		LiteralArgumentBuilder<ServerCommandSource> resetArgument = CommandManager.literal("reset");
		LiteralArgumentBuilder<ServerCommandSource> setArgument = CommandManager.literal("set");
		RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = CommandManager.argument("line",
				IntegerArgumentType.integer(0, 10));
		RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = CommandManager
				.argument("name...", StringArgumentType.greedyString()).executes(context -> {
					return changeLore(context, IntegerArgumentType.getInteger(context, "line"));
				});

		builder.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.item.lore", 2));

		resetArgument.executes(context -> {
			ItemStack item = context.getSource().getPlayer().getMainHandStack();
			CompoundTag itemTag = item.getTag();

			if (item == null || item.isEmpty() == true) {
				context.getSource().sendFeedback(LangText.get(true, "command.item.name.noitem"), false);
			} else {
				if (itemTag == null || !itemTag.contains("lore") || !itemTag.getCompound("display").contains("Lore")) {
					return 1;
				} else {
					itemTag.getCompound("display").remove("Lore");
					context.getSource().sendFeedback(LangText.get(true, "command.item.lore.reset.success"), false);
				}
			}

			return 1;
		});

		builder.then(resetArgument);
		lineArgument.then(nameArgument);
		setArgument.then(lineArgument);
		builder.then(setArgument);
		argumentBuilder.then(builder);
	}

	public static int changeLore(CommandContext<ServerCommandSource> context, int line) throws CommandSyntaxException {
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

			CompoundTag itemTag = item.getTag();
			if (item.hasTag() == false || itemTag == null) {
				itemTag = new CompoundTag();
			}

			if (!itemTag.contains("display")) {
				itemTag.put("display", new CompoundTag());
			}

			ListTag lore = itemTag.getCompound("display").getList("Lore", 8);
			if (lore == null) {
				lore = new ListTag();
			}

			if (line > lore.size() - 1) {
				for (int i = lore.size(); i <= line; i++) {
					lore.add(StringTag.of("{\"text\":\"\"}"));
				}
			}

			String text = StringArgumentType.getString(context, "name...");
			if (Thimble.hasPermissionOrOp(context.getSource(), "kiloessentials.command.item.lore.colour", 2)) {
				text = TextFormat.translateAlternateColorCodes('&', text);
			} else {
				text = TextFormat.removeAlternateColorCodes('&', text);
			}

			lore.set(line, StringTag.of("{\"text\":\"" + text + "\"}"));
			itemTag.getCompound("display").put("Lore", lore);
			item.setTag(itemTag);

			player.sendMessage(LangText.getFormatter(true, "command.item.lore.success", line,
					StringArgumentType.getString(context, "name...")));
		}

		return 1;
	}
}
