//package org.kilocraft.essentials.craft.commands.essentials;
//
//import com.mojang.brigadier.CommandDispatcher;
//import com.mojang.brigadier.arguments.StringArgumentType;
//import com.mojang.brigadier.context.CommandContext;
//import com.mojang.brigadier.tree.ArgumentCommandNode;
//import com.mojang.brigadier.tree.LiteralCommandNode;
//import io.github.indicode.fabric.permissions.Thimble;
//import net.minecraft.command.EntitySelector;
//import net.minecraft.command.arguments.EntityArgumentType;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.server.command.CommandManager;
//import net.minecraft.server.command.ServerCommandSource;
//import net.minecraft.text.LiteralText;
//import org.kilocraft.essentials.api.chat.LangText;
//import org.kilocraft.essentials.api.chat.TextFormat;
//import org.kilocraft.essentials.craft.KiloCommands;
//import org.kilocraft.essentials.craft.player.KiloPlayerManager;
//
//public class NickCommand {
//	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
//		KiloCommands.getCommandPermission("nick");
//		KiloCommands.getCommandPermission("nick.self");
//		KiloCommands.getCommandPermission("nick.others");
//		LiteralCommandNode<ServerCommandSource> nick = CommandManager.literal("nick")
//				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.self"), 2)).build();
//
//		LiteralCommandNode<ServerCommandSource> set = CommandManager.literal("set")
//				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.self"), 2)).build();
//
//		LiteralCommandNode<ServerCommandSource> reset = CommandManager.literal("reset")
//				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.self"), 2))
//				.executes(context -> {
//					resetNick(context, context.getSource().getPlayer());
//					return 0;
//				}).build();
//
//		ArgumentCommandNode<ServerCommandSource, String> name = CommandManager
//				.argument("name", StringArgumentType.string()).executes(context -> {
//					// KiloPlayer.get(player).setNickname(name);
//					changeNick(context, context.getSource().getPlayer());
//
//					return 0;
//				}).build();
//
//		ArgumentCommandNode<ServerCommandSource, EntitySelector> nameTarget = CommandManager
//				.argument("target", EntityArgumentType.player())
//				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.others"), 2))
//				.executes(context -> {
//					changeNick(context, EntityArgumentType.getPlayer(context, "target"));
//					return 0;
//				}).build();
//
//		ArgumentCommandNode<ServerCommandSource, EntitySelector> resetTarget = CommandManager
//				.argument("target", EntityArgumentType.player())
//				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.others"), 2))
//				.executes(context -> {
//					resetNick(context, EntityArgumentType.getPlayer(context, "target"));
//					return 0;
//				}).build();
//
//		name.addChild(nameTarget);
//		reset.addChild(resetTarget);
//		set.addChild(name);
//		nick.addChild(set);
//		nick.addChild(reset);
//		dispatcher.getRoot().addChild(nick);
//	}
//
//	private static void changeNick(CommandContext<ServerCommandSource> context, PlayerEntity player) {
//		String nick = StringArgumentType.getString(context, "name");
//		KiloPlayerManager.getPlayerData(player.getUuid()).nick = new LiteralText(
//				TextFormat.translateAlternateColorCodes('&', nick)).asString();
//		context.getSource().sendFeedback(LangText.getFormatter(true, "command.nick.success",
//				player.getName().asString(), TextFormat.removeAlternateColorCodes('&', nick)), false);
//	}
//
//	private static void resetNick(CommandContext<ServerCommandSource> context, PlayerEntity player) {
//		KiloPlayerManager.getPlayerData(player.getUuid()).nick = "";
//		context.getSource().sendFeedback(LangText.getFormatter(true, "command.nick.reset", player.getName().asString()),
//				false);
//	}
//}
