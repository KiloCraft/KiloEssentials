package org.kilocraft.essentials.craft.commands.essentials;
//package org.kilocraft.essentials.craft.commands.donatorcommands;
//
//import com.mojang.brigadier.CommandDispatcher;
//import com.mojang.brigadier.context.CommandContext;
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import com.mojang.brigadier.tree.LiteralCommandNode;
//import net.minecraft.server.command.CommandManager;
//import net.minecraft.server.command.ServerCommandSource;
//import org.kilocraft.essentials.api.chat.LangText;
//import org.kilocraft.essentials.craft.player.KiloPlayerManager;
//
//public class PlayerParticlesCommand {
//
//	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
//		LiteralCommandNode<ServerCommandSource> particlesNode = CommandManager.literal("playerparticles")
//				.executes((context) -> {
//					context.getSource().sendFeedback(LangText.get(true, "command.playerparticles.onlyoneargument"),
//							false);
//					return 0;
//				}).build();
//
//		LiteralCommandNode<ServerCommandSource> setNode = CommandManager.literal("set").executes((context) -> {
//			context.getSource().sendFeedback(LangText.get(true, "command.playerparticles.noparticleschosen"), false);
//			return 0;
//		}).build();
//
//		// Particles
//		LiteralCommandNode<ServerCommandSource> flamesNode = CommandManager.literal("flames").executes((context) -> {
//			setParticle(context, "flames");
//			return 0;
//		}).build();
//
//		LiteralCommandNode<ServerCommandSource> glassNode = CommandManager.literal("glass").executes((context) -> {
//			setParticle(context, "glass");
//			return 0;
//		}).build();
//
//		LiteralCommandNode<ServerCommandSource> rainNode = CommandManager.literal("rain").executes((context) -> {
//			setParticle(context, "rain");
//			return 0;
//		}).build();
//
//		LiteralCommandNode<ServerCommandSource> disableNode = CommandManager.literal("disable").executes((context) -> {
//			KiloPlayer kiloPlayer = KiloPlayerManager.getPlayerData(context.getSource().getPlayer().getUuid());
//			kiloPlayer.particle = "";
//			context.getSource().sendFeedback(LangText.get(true, "command.playerparticles.disable"), false);
//			return 0;
//		}).build();
//
//		setNode.addChild(flamesNode);
//		setNode.addChild(glassNode);
//		setNode.addChild(rainNode);
//
//		particlesNode.addChild(setNode);
//		particlesNode.addChild(disableNode);
//		dispatcher.getRoot().addChild(particlesNode);
//		dispatcher.getRoot().addChild(CommandManager.literal("pp").redirect(particlesNode).build());
//	}
//
//	private static void setParticle(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
//		KiloPlayer kiloPlayer = KiloPlayerManager.getPlayerData(context.getSource().getPlayer().getUuid());
//		kiloPlayer.particle = name;
//		((ServerCommandSource) context.getSource())
//				.sendFeedback(LangText.getFormatter(true, "command.playerparticles.particleset", name), false);
//	}
//
//}
