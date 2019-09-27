package org.kilocraft.essentials.craft.commands.essentials.ItemCommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.LangText;

public class ItemLoreCommand {
	public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder) {
		/*
		 * Thimble.permissionWriters.add(pair -> { try {
		 * Thimble.PERMISSIONS.getPermission("kiloessentials.command.item.lore",
		 * CommandPermission.class); // Permission that updates command tree } catch
		 * (NoSuchMethodException | InstantiationException | InvocationTargetException |
		 * IllegalAccessException e) { e.printStackTrace(); } });
		 */
		LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager
				.literal("lore")/*
								 * .requires(source -> Thimble.hasPermissionChildOrOp(source,
								 * "kiloessentials.command.item.lore", 3))
								 */;

		LiteralArgumentBuilder<ServerCommandSource> resetArgument = CommandManager.literal("reset");
		LiteralArgumentBuilder<ServerCommandSource> setArgument = CommandManager.literal("set");
		RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = CommandManager.argument("line",
				IntegerArgumentType.integer(0, 10));

		RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = CommandManager
				.argument("name...", StringArgumentType.greedyString()).executes(context -> {
					return changeLore(context, IntegerArgumentType.getInteger(context, "line"));
				});

		resetArgument.executes(context -> {
			ItemStack item = context.getSource().getPlayer().getMainHandStack();
			CompoundTag itemTag = item.getTag();

			if (item == null || item.isEmpty() == true) {
				context.getSource().sendFeedback(LangText.get(true, "command.item.name.noitem"), false);
			} else {
				if (itemTag == null || !itemTag.containsKey("display")
						|| !itemTag.getCompound("display").containsKey("Lore")) {
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

			if (!itemTag.containsKey("display")) {
				itemTag.put("display", new CompoundTag());
			}

			ListTag lore = itemTag.getCompound("display").getList("Lore", 8);
			if (lore == null) {
				lore = new ListTag();
			}

			if (line > lore.size() - 1) {
				for (int i = lore.size(); i <= line; i++) {
					lore.add(new StringTag("{\"text\":\"\"}"));
				}
			}

			lore.set(line, new StringTag("{\"text\":\"" + StringArgumentType.getString(context, "name...") + "\"}"));
			itemTag.getCompound("display").put("Lore", lore);
			item.setTag(itemTag);

			player.sendMessage(LangText.getFormatter(true, "command.item.lore.success", line,
					StringArgumentType.getString(context, "name...")));
		}

		return 1;
	}
}
