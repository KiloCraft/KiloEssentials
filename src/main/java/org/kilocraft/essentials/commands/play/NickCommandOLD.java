package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.client.network.packet.PlayerListS2CPacket;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.KiloCommands;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NickCommandOLD {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		KiloCommands.getCommandPermission("nick");
		KiloCommands.getCommandPermission("nick.self");
		KiloCommands.getCommandPermission("nick.others");
		LiteralCommandNode<ServerCommandSource> nick = literal("nick")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.self"), 2)).build();

		LiteralCommandNode<ServerCommandSource> set = literal("set")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.self"), 2)).build();

		LiteralCommandNode<ServerCommandSource> reset = literal("reset")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.self"), 2))
				.executes(context -> {
					resetNick(context, context.getSource().getPlayer());
					return 0;
				}).build();

		ArgumentCommandNode<ServerCommandSource, String> name = argument("name", string()).executes(context -> {
					changeNick(context, context.getSource().getPlayer());
					return 0;
				}).build();

		ArgumentCommandNode<ServerCommandSource, EntitySelector> nameTarget = argument("target", player())
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.others"), 2))
				.executes(context -> {
					changeNick(context, getPlayer(context, "target"));
					return 0;
				}).build();

		ArgumentCommandNode<ServerCommandSource, EntitySelector> resetTarget = argument("target", player())
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.others"), 2))
				.executes(context -> {
					resetNick(context, getPlayer(context, "target"));
					return 0;
				}).build();

		name.addChild(nameTarget);
		reset.addChild(resetTarget);
		set.addChild(name);
		nick.addChild(set);
		nick.addChild(reset);
		dispatcher.getRoot().addChild(nick);
	}

	private static void changeNick(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
		String nick = getString(context, "name");
		player.setCustomName(new LiteralText(TextFormat.translateAlternateColorCodes('&', nick)));

		context.getSource().sendFeedback(LangText.getFormatter(true, "command.nick.success",
				player.getName().asString(), TextFormat.removeAlternateColorCodes('&', nick)), false);
		KiloServer.getServer().getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
	}

	private static void resetNick(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
		player.setCustomName(player.getDisplayName());
		context.getSource().sendFeedback(LangText.getFormatter(true, "command.nick.reset", player.getName().asString()),
				false);
		KiloServer.getServer().getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
	}
}
