package org.kilocraft.essentials.craft.commands.essentials;

import java.util.Date;
import org.kilocraft.essentials.craft.KiloCommands;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.command.arguments.MessageArgumentType;
import net.minecraft.command.arguments.MessageArgumentType.MessageFormat;
import net.minecraft.command.arguments.GameProfileArgumentType.GameProfileArgument;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;

public class BanCommand {
	private static final SimpleCommandExceptionType ALREADY_BANNED_EXCEPTION = new SimpleCommandExceptionType(
			new TranslatableText("commands.ban.failed", new Object[0]));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> ban = CommandManager.literal("ban")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("ban"), 2));
		RequiredArgumentBuilder<ServerCommandSource, GameProfileArgument> player = CommandManager.argument("target",
				GameProfileArgumentType.gameProfile());
		RequiredArgumentBuilder<ServerCommandSource, MessageFormat> reason = CommandManager.argument("reason",
				MessageArgumentType.message());
		RequiredArgumentBuilder<ServerCommandSource, Integer> time = CommandManager.argument("time",
				IntegerArgumentType.integer(0, 365));

		LiteralArgumentBuilder<ServerCommandSource> minutes = CommandManager.literal("minutes");
		LiteralArgumentBuilder<ServerCommandSource> hours = CommandManager.literal("hours");
		LiteralArgumentBuilder<ServerCommandSource> days = CommandManager.literal("days");

		reason.executes(context -> {
			GameProfile target = (GameProfile) GameProfileArgumentType.getProfileArgument(context, "target")
					.toArray()[0];
			return ban(context.getSource(), target, MessageArgumentType.getMessage(context, "reason"), null);
		});

		minutes.executes(context -> {
			GameProfile target = (GameProfile) GameProfileArgumentType.getProfileArgument(context, "target")
					.toArray()[0];
			Date date = new Date();
			date = Date.from(date.toInstant().plusSeconds(IntegerArgumentType.getInteger(context, "time") * 60));
			return ban(context.getSource(), target, MessageArgumentType.getMessage(context, "reason"), date);
		});

		hours.executes(context -> {
			GameProfile target = (GameProfile) GameProfileArgumentType.getProfileArgument(context, "target")
					.toArray()[0];
			Date date = new Date();
			date = Date.from(date.toInstant().plusSeconds(IntegerArgumentType.getInteger(context, "time") * 720));
			return ban(context.getSource(), target, MessageArgumentType.getMessage(context, "reason"), date);
		});

		days.executes(context -> {
			GameProfile target = (GameProfile) GameProfileArgumentType.getProfileArgument(context, "target")
					.toArray()[0];
			Date date = new Date();
			date = Date.from(date.toInstant().plusSeconds(IntegerArgumentType.getInteger(context, "time") * 17280));
			return ban(context.getSource(), target, MessageArgumentType.getMessage(context, "reason"), date);
		});

		time.then(minutes);
		time.then(hours);
		time.then(days);
		reason.then(time);
		player.then(ban);
		ban.then(player);
		dispatcher.register(ban);
	}

	private static int ban(ServerCommandSource serverCommandSource_1, GameProfile target, Text text_1, Date date)
			throws CommandSyntaxException {
		BannedPlayerList bannedPlayerList_1 = serverCommandSource_1.getMinecraftServer().getPlayerManager()
				.getUserBanList();

		// TODO: Set correct messages
		if (!bannedPlayerList_1.contains(target)) {
			BannedPlayerEntry bannedPlayerEntry_1 = new BannedPlayerEntry(target, date, serverCommandSource_1.getName(),
					date, text_1.getString());
			bannedPlayerList_1.add(bannedPlayerEntry_1);

			if (date == null) {
				serverCommandSource_1.sendFeedback(new TranslatableText("commands.ban.success",
						new Object[] { Texts.toText(target), bannedPlayerEntry_1.getReason() }), true);
			} else {
				serverCommandSource_1.sendFeedback(new TranslatableText("commands.ban.success.temp",
						new Object[] { Texts.toText(target), bannedPlayerEntry_1.getReason() }), true);
			}

			ServerPlayerEntity serverPlayerEntity_1 = serverCommandSource_1.getMinecraftServer().getPlayerManager()
					.getPlayer(target.getId());
			if (serverPlayerEntity_1 != null) {
				if (date == null) {
					serverPlayerEntity_1.networkHandler
							.disconnect(new TranslatableText("multiplayer.disconnect.banned", new Object[0]));
				} else {
					serverPlayerEntity_1.networkHandler
							.disconnect(new TranslatableText("multiplayer.disconnect.banned.temp", new Object[0]));
				}
			}
		} else {
			throw ALREADY_BANNED_EXCEPTION.create();
		}

		return 0;
	}
}
