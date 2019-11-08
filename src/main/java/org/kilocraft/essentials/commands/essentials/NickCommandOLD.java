package org.kilocraft.essentials.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.client.network.packet.PlayerListS2CPacket;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.KiloCommands;

public class NickCommandOLD {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		KiloCommands.getCommandPermission("nick");
		KiloCommands.getCommandPermission("nick.self");
		KiloCommands.getCommandPermission("nick.others");
		LiteralCommandNode<ServerCommandSource> nick = CommandManager.literal("nick")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.self"), 2)).build();

		LiteralCommandNode<ServerCommandSource> set = CommandManager.literal("set")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.self"), 2)).build();

		LiteralCommandNode<ServerCommandSource> reset = CommandManager.literal("reset")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.self"), 2))
				.executes(context -> {
					resetNick(context, context.getSource().getPlayer());
					return 0;
				}).build();

		ArgumentCommandNode<ServerCommandSource, String> name = CommandManager
				.argument("name", StringArgumentType.string()).executes(context -> {
					changeNick(context, context.getSource().getPlayer());
					return 0;
				}).build();

		ArgumentCommandNode<ServerCommandSource, EntitySelector> nameTarget = CommandManager
				.argument("target", EntityArgumentType.player())
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.others"), 2))
				.executes(context -> {
					changeNick(context, EntityArgumentType.getPlayer(context, "target"));
					return 0;
				}).build();

		ArgumentCommandNode<ServerCommandSource, EntitySelector> resetTarget = CommandManager
				.argument("target", EntityArgumentType.player())
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.others"), 2))
				.executes(context -> {
					resetNick(context, EntityArgumentType.getPlayer(context, "target"));
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
		String nick = StringArgumentType.getString(context, "name");
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
