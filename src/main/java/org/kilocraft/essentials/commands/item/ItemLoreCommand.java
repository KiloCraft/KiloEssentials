package org.kilocraft.essentials.commands.item;

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
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextFormat;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ItemLoreCommand {
	public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder) {
		LiteralArgumentBuilder<ServerCommandSource> builder = literal("lore")
				.requires(src -> KiloCommands.hasPermission(src, CommandPermission.ITEM_LORE));
		LiteralArgumentBuilder<ServerCommandSource> resetArgument = literal("reset");
		LiteralArgumentBuilder<ServerCommandSource> setArgument = literal("set");
		RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = argument("line", integer(1, 10));
		RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name...", greedyString()).executes(context ->
				changeLore(context, getInteger(context, "line")));

		builder.requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.item.lore", 2));

		resetArgument.executes(context -> {
			ItemStack item = context.getSource().getPlayer().getMainHandStack();
			CompoundTag itemTag = item.getTag();

			if (item == ItemStack.EMPTY || item.isEmpty()) {
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

	public static int changeLore(CommandContext<ServerCommandSource> context, int inputLine) throws CommandSyntaxException {
		PlayerEntity player = context.getSource().getPlayer();
		ItemStack item = player.getMainHandStack();

		if (item == null || item.isEmpty() == true) {
			context.getSource().sendFeedback(LangText.get(true, "command.item.name.noitem"), false);
		} else {
			if (player.experienceLevel < 1 && !player.isCreative()) {
				context.getSource().sendFeedback(LangText.get(true, "command.item.name.noxp"), false);
				return 1;
			}

			if (!player.isCreative()) {
				player.addExperienceLevels(-1);
			}

			CompoundTag itemTag = item.getTag();
			if (!item.hasTag() || itemTag == null) {
				itemTag = new CompoundTag();
			}

			if (!itemTag.contains("display")) {
				itemTag.put("display", new CompoundTag());
			}

			ListTag lore = itemTag.getCompound("display").getList("Lore", 8);
			if (lore == null) {
				lore = new ListTag();
			}

			if (inputLine > lore.size() - 1) {
				for (int i = lore.size(); i <= inputLine; i++) {
					lore.add(StringTag.of("{\"text\":\"\"}"));
				}
			}

			String text = getString(context, "name...");
			if (KiloCommands.hasPermission(context.getSource(), CommandPermission.ITEM_FORMATTING)) {
				text = TextFormat.translateAlternateColorCodes('&', text);
			} else {
				text = TextFormat.removeAlternateColorCodes('&', text);
			}

			lore.set(inputLine, StringTag.of("{\"text\":\"" + text + "\"}"));
			itemTag.getCompound("display").put("Lore", lore);
			item.setTag(itemTag);

			player.sendMessage(LangText.getFormatter(true, "command.item.lore.success", inputLine,
					getString(context, "name...")));
		}
		return 1;
	}
}
