package org.kilocraft.essentials.craft.commands.essentials.donatorcommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.craft.user.User;

public class PlayerParticlesCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> particlesNode = CommandManager.literal("playerparticles")
				.executes((context) -> {
					context.getSource().sendFeedback(LangText.get(true, "command.playerparticles.onlyoneargument"),
							false);
					return 0;
				}).build();

		LiteralCommandNode<ServerCommandSource> setNode = CommandManager.literal("set").executes((context) -> {
			context.getSource().sendFeedback(LangText.get(true, "command.playerparticles.noparticleschosen"), false);
			return 0;
		}).build();

		// Particles
		LiteralCommandNode<ServerCommandSource> flamesNode = CommandManager.literal("flames").executes((context) -> {
			setParticle(context, "flames", 1);
			return 0;
		}).build();

		LiteralCommandNode<ServerCommandSource> glassNode = CommandManager.literal("glass").executes((context) -> {
			setParticle(context, "glass", 2);
			return 0;
		}).build();

		LiteralCommandNode<ServerCommandSource> rainNode = CommandManager.literal("rain").executes((context) -> {
			setParticle(context, "rain", 3);
			return 0;
		}).build();

		LiteralCommandNode<ServerCommandSource> disableNode = CommandManager.literal("disable").executes((context) -> {
			User user = KiloServer.getServer().getUserManager().getUser(context.getSource().getPlayer().getUuid());
			user.setDisplayParticleId(0);
			context.getSource().sendFeedback(LangText.get(true, "command.playerparticles.disable"), false);
			return 0;
		}).build();

		setNode.addChild(flamesNode);
		setNode.addChild(glassNode);
		setNode.addChild(rainNode);

		particlesNode.addChild(setNode);
		particlesNode.addChild(disableNode);
		dispatcher.getRoot().addChild(particlesNode);
		dispatcher.getRoot().addChild(CommandManager.literal("pp").redirect(particlesNode).build());
	}

	private static void setParticle(CommandContext<ServerCommandSource> context, String name, int id) throws CommandSyntaxException {
		User user = KiloServer.getServer().getUserManager().getUser(context.getSource().getPlayer().getUuid());
		user.setDisplayParticleId(id);
		((ServerCommandSource) context.getSource())
				.sendFeedback(LangText.getFormatter(true, "command.playerparticles.particleset", name), false);
	}

}
