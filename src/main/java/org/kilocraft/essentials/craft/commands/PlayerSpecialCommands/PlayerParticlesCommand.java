package org.kilocraft.essentials.craft.commands.PlayerSpecialCommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.Util.LangText;

public class PlayerParticlesCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> particlesNode = CommandManager.literal("particles").executes((context) -> {
			context.getSource().sendFeedback(LangText.get(true, "command.playerparticles.onlyoneargument"), false);
			return 0;
		}).build();
		
		LiteralCommandNode<ServerCommandSource> setNode = CommandManager.literal("set").executes((context) -> {
			context.getSource().sendFeedback(LangText.get(true, "command.playerparticles.noparticleschosen"), false);
			return 0;
		}).build();
		
		// Particles
		LiteralCommandNode<ServerCommandSource> flamesNode = CommandManager.literal("flames").executes((context) -> {
			setParticle(context, "flames");
			return 0;
		}).build();
		
		LiteralCommandNode<ServerCommandSource> disableNode = CommandManager.literal("disable").executes((context) -> {
			// TODO: Disable particles
			context.getSource().sendFeedback(LangText.get(true, "command.playerparticles.disable"), false);
			return 0;
		}).build();
		
		dispatcher.getRoot().addChild(particlesNode);
		particlesNode.addChild(setNode);
		particlesNode.addChild(disableNode);
		setNode.addChild(flamesNode);
	}

	private static void setParticle(CommandContext<ServerCommandSource> context, String name) {
		((ServerCommandSource) context.getSource())
				.sendFeedback(LangText.getFormatter(true, "command.playerparticles.particleset", name), false);
	}

}
