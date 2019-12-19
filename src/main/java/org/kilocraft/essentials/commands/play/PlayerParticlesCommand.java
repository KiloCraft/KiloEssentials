package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;

import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.user.User;

import static net.minecraft.server.command.CommandManager.literal;

public class PlayerParticlesCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> particlesNode = literal("playerparticles")
				.requires(src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.PLAYERPARTICLE))
				.executes(KiloCommands::executeSmartUsage).build();

		LiteralCommandNode<ServerCommandSource> setNode = literal("set").build();

		// Particles
		LiteralCommandNode<ServerCommandSource> flamesNode = literal("flames").executes((context) -> {
			setParticle(context, "flames", 1);
			return 0;
		}).build();

		LiteralCommandNode<ServerCommandSource> glassNode = literal("glass").executes((context) -> {
			setParticle(context, "glass", 2);
			return 0;
		}).build();

		LiteralCommandNode<ServerCommandSource> rainNode = literal("rain").executes((context) -> {
			setParticle(context, "rain", 3);
			return 0;
		}).build();

		LiteralCommandNode<ServerCommandSource> disableNode = literal("disable").executes((context) -> {
			User serverUser = KiloServer.getServer().getUserManager().getOnline(context.getSource().getPlayer().getUuid());
			serverUser.setDisplayParticleId(0);
			context.getSource().sendFeedback(LangText.get(true, "command.playerparticles.disable"), false);
			return 0;
		}).build();

		setNode.addChild(flamesNode);
		setNode.addChild(glassNode);
		setNode.addChild(rainNode);

		particlesNode.addChild(setNode);
		particlesNode.addChild(disableNode);
		dispatcher.getRoot().addChild(particlesNode);
		dispatcher.getRoot().addChild(literal("pp").redirect(particlesNode).build());
	}

	private static void setParticle(CommandContext<ServerCommandSource> context, String name, int id) throws CommandSyntaxException {
		User user = KiloServer.getServer().getUserManager().getOnline(context.getSource().getPlayer().getUuid());
		user.setDisplayParticleId(id);
		context.getSource().sendFeedback(LangText.getFormatter(true, "command.playerparticles.particleset", name), false);
	}

}
