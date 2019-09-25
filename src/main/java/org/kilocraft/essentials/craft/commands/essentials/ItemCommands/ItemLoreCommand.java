package org.kilocraft.essentials.craft.commands.essentials.ItemCommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.indicode.fabric.permissions.Thimble;
import io.github.indicode.fabric.permissions.command.CommandPermission;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.LangText;

import java.lang.reflect.InvocationTargetException;

public class ItemLoreCommand {
	public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder) {
		/*Thimble.permissionWriters.add(pair -> {
			try {
				Thimble.PERMISSIONS.getPermission("kiloessentials.command.item.lore", CommandPermission.class); // Permission that updates command tree
			} catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});*/
		LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("lore")/*
				.requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.item.lore", 3))*/;

		LiteralArgumentBuilder<ServerCommandSource> resetArgument = CommandManager.literal("reset");
		LiteralArgumentBuilder<ServerCommandSource> setArgument = CommandManager.literal("set");
		RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = CommandManager
				.argument("line", IntegerArgumentType.integer(0, 10)).executes(context -> {
					return changeLore(context, IntegerArgumentType.getInteger(context, "line"));
				});

		RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = CommandManager
				.argument("name...", StringArgumentType.greedyString()).executes(context -> {
					return changeLore(context, 0);
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
				context.getSource().sendFeedback(LangText.get(true, "command.item.name.noitem"), false);
			} else {
				if (itemTag == null || !itemTag.containsKey("display")
						|| !itemTag.getCompound("display").containsKey("Lore")) {
					context.getSource().sendFeedback(LangText.get(true, "command.item.lore.nolore"), false);
				} else {
					System.out.println(itemTag.getCompound("display").getType("Lore"));
					itemTag.getCompound("display").remove("Lore");
					context.getSource().sendFeedback(LangText.get(true, "command.item.lore.reset.success"), false);
				}
			}

			return 1;
		});
	}

	public static int changeLore(CommandContext<ServerCommandSource> context, int line) throws CommandSyntaxException {
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

			CompoundTag itemTag = item.getTag();
			if (!itemTag.containsKey("display")) {
				CompoundTag displayTag = new CompoundTag();
				displayTag.putString("Lore", StringArgumentType.getString(context, "name..."));
				item.getTag().put("display", displayTag);
			} else {
				item.getTag().getCompound("display").putString("Lore",
						StringArgumentType.getString(context, "name..."));
			}

			player.sendMessage(LangText.getFormatter(true, "command.item.lore.success",
					StringArgumentType.getString(context, "name...")));
		}

		return 1;
	}
}
