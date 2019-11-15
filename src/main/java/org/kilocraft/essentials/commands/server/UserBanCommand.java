package org.kilocraft.essentials.commands.server;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.GameProfileArgumentType.GameProfileArgument;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.KiloCommands;

import java.util.Date;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.arguments.GameProfileArgumentType.gameProfile;
import static net.minecraft.command.arguments.GameProfileArgumentType.getProfileArgument;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class UserBanCommand {
	private static final SimpleCommandExceptionType ALREADY_BANNED_EXCEPTION = new SimpleCommandExceptionType(
			new TranslatableText("commands.ban.failed"));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> ban = literal("ke_ban")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("ban"), 2));
		RequiredArgumentBuilder<ServerCommandSource, GameProfileArgument> player = argument("target",
				gameProfile());
		RequiredArgumentBuilder<ServerCommandSource, String> reason = argument("reason",
				string());
		RequiredArgumentBuilder<ServerCommandSource, Integer> time = argument("time",
				integer(0, 365));

		LiteralArgumentBuilder<ServerCommandSource> minutes = literal("minutes");
		LiteralArgumentBuilder<ServerCommandSource> hours = literal("hours");
		LiteralArgumentBuilder<ServerCommandSource> days = literal("days");

		reason.executes(context -> {
			GameProfile target = (GameProfile) getProfileArgument(context, "target")
					.toArray()[0];
			return ban(context.getSource(), target, new LiteralText(getString(context, "reason")), null);
		});

		minutes.executes(context -> {
			GameProfile target = (GameProfile) getProfileArgument(context, "target")
					.toArray()[0];
			Date date = new Date();
			date = Date.from(date.toInstant().plusSeconds(getInteger(context, "time") * 60));
			return ban(context.getSource(), target, new LiteralText(getString(context, "reason")), date);
		});

		hours.executes(context -> {
			GameProfile target = (GameProfile) getProfileArgument(context, "target")
					.toArray()[0];
			Date date = new Date();
			date = Date.from(date.toInstant().plusSeconds(getInteger(context, "time") * 720));
			return ban(context.getSource(), target, new LiteralText(getString(context, "reason")), date);
		});

		days.executes(context -> {
			GameProfile target = (GameProfile) getProfileArgument(context, "target")
					.toArray()[0];
			Date date = new Date();
			date = Date.from(date.toInstant().plusSeconds(getInteger(context, "time") * 17280));
			return ban(context.getSource(), target, new LiteralText(getString(context, "reason")), date);
		});

		time.then(minutes);
		time.then(hours);
		time.then(days);
		reason.then(time);
		player.then(reason);
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
