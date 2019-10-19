package org.kilocraft.essentials.craft.commands.essentials;

import java.util.Random;

import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.craft.player.KiloPlayer;
import org.kilocraft.essentials.craft.player.KiloPlayerManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class RandomTeleportCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> randomTeleport = CommandManager.literal("randomteleport");
		LiteralArgumentBuilder<ServerCommandSource> rtp = CommandManager.literal("rtp");
		RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("target",
				EntityArgumentType.player());

		rtp.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.rtp.self", 2));
		target.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.rtp.others", 2));

		rtp.executes(context -> {
			teleportRandomly(context.getSource().getPlayer());
			return 0;
		});

		target.executes(context -> {
			teleportRandomly(EntityArgumentType.getPlayer(context, "target"));
			return 0;
		});

		randomTeleport.then(target);
		rtp.then(target);
		dispatcher.register(randomTeleport);
		dispatcher.register(rtp);
	}

	private static void teleportRandomly(ServerPlayerEntity player) {
		KiloPlayer kiloPlayer = KiloPlayerManager.getPlayerData(player.getUuid());
		if (kiloPlayer.rtpLeft == 0) {
			player.sendMessage(LangText.get(true, "command.randomteleport.runout"));
		} else {
			Random random = new Random();
			int randomX = random.nextInt(9999000) + 1000; // 1000 - 10000000
			int randomZ = random.nextInt(9999000) + 1000; // 1000 - 10000000

			// Negative coords as well
			if (random.nextInt(2) == 0) {
				randomX *= -1;
			}

			if (random.nextInt(2) == 0) {
				randomZ *= -1;
			}

			player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 30, 255, false, true, true));
			player.teleport(randomX, 255, randomZ);
			kiloPlayer.rtpLeft -= 1;
			player.sendMessage(
					LangText.getFormatter(true, "command.randomteleport.success", "X: " + randomX + ", Z: " + randomZ, kiloPlayer.rtpLeft));
		}
	}
}
