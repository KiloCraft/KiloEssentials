package org.kilocraft.essentials.commands.PlayerSpecialCommands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.commands.CommandArguments.PlayerParticlesCommandArgument;
import org.kilocraft.essentials.utils.LangText;

public class PlayerParticlesCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("particles").executes(context -> {
			context.getSource().sendFeedback(LangText.getFormatter(true, "command.playerparticles.onlyoneargument"),
					false);
			return 1;
		}).then(CommandManager.literal("set").executes(context -> {
			context.getSource().sendFeedback(LangText.getFormatter(true, "command.playerparticles.noparticleschosen"),
					false);
			return 1;
		}).then(CommandManager.argument("name", PlayerParticlesCommandArgument.particles()).executes(context -> {
			// TODO: Change particle
			String particle = PlayerParticlesCommandArgument.getParticleName(context, "name");
			if (PlayerParticlesCommandArgument.NAMES.contains(particle)) {
				context.getSource().sendFeedback(LangText.getFormatter(true, "command.playerparticles.particleset"),
						false);
				return 0;
			} else {
				context.getSource()
						.sendFeedback(LangText.getFormatter(true, "command.playerparticles.incorrectparticle"), false);
				return 1;
			}
		}))).then(CommandManager.literal("disable").executes(context -> {
			// TODO: Disable particles
			context.getSource().sendFeedback(LangText.getFormatter(true, "command.playerparticles.disable"), false);
			return 0;
		})));
	}

}
