package org.kilocraft.essentials.commands.PlayerSpecialCommands;

import org.kilocraft.essentials.Mod;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.commands.CommandArguments.PlayerParticlesCommandArgument;

public class PlayerParticlesCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("particles").executes(context -> {
			context.getSource()
					.sendFeedback(new LiteralText(Mod.lang.getProperty("command.playerparticles.onlyoneargument"))
							.setStyle(new Style().setColor(Formatting.RED)), false);
			return 1;
		}).then(CommandManager.literal("set").executes(context -> {
			context.getSource()
					.sendFeedback(new LiteralText(Mod.lang.getProperty("command.playerparticles.noparticleschosen"))
							.setStyle(new Style().setColor(Formatting.RED)), false);
			return 1;
		}).then(CommandManager.argument("name", PlayerParticlesCommandArgument.particles()).executes(context -> {
			// TODO: Change particle
			String particle = PlayerParticlesCommandArgument.getParticleName(context, "name");
			if (PlayerParticlesCommandArgument.NAMES.contains(particle)) {
				context.getSource().sendFeedback(
						new LiteralText(Mod.lang.getProperty("command.playerparticles.particleset")), false);
				return 0;
			} else {
				context.getSource().sendFeedback(
						new LiteralText(Mod.lang.getProperty("command.playerparticles.incorrectparticle"))
								.setStyle(new Style().setColor(Formatting.RED)),
						false);
				return 1;
			}
		}))).then(CommandManager.literal("disable").executes(context -> {
			// TODO: Disable particles
			context.getSource().sendFeedback(new LiteralText(Mod.lang.getProperty("command.playerparticles.disable")),
					false);
			return 0;
		})));
	}

}
