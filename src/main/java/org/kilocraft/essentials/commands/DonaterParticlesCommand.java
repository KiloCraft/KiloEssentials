package org.kilocraft.essentials.commands;

import org.kilocraft.essentials.Mod;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class DonaterParticlesCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("donatorparticles").executes(context -> {
			context.getSource()
					.sendFeedback(new LiteralText(Mod.messages.getProperty("command.donatorparticles.onlyoneargument"))
							.setStyle(new Style().setColor(Formatting.RED)), false);
			return 1;
		}).then(CommandManager.literal("set").executes(context -> {
			context.getSource().sendFeedback(
					new LiteralText(Mod.messages.getProperty("command.donatorparticles.noparticleschosen"))
							.setStyle(new Style().setColor(Formatting.RED)),
					false);
			return 1;
		}).then(CommandManager.argument("name", DonatorParticlesCommandArgument.particles()).executes(context -> {
			// TODO: Change particle
			context.getSource().sendFeedback(
					new LiteralText(Mod.messages.getProperty("command.donatorparticles.particleset")), false);
			return 0;
		})).then(CommandManager.literal("disable")).executes(context -> {
			// TODO: Disable particles
			context.getSource()
					.sendFeedback(new LiteralText(Mod.messages.getProperty("command.donatorparticles.disable")), false);
			return 0;
		})));
	}

}
