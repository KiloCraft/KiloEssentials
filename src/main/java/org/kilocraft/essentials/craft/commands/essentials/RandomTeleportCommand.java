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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome.Category;

public class RandomTeleportCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> randomTeleport = CommandManager.literal("randomteleport");
		RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("target",
				EntityArgumentType.player());

		randomTeleport.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.rtp.self", 2));
		target.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.rtp.others", 2));

		randomTeleport.executes(context -> {
			teleportRandomly(context.getSource().getPlayer(), context.getSource());
			return 0;
		});

		target.executes(context -> {
			teleportRandomly(EntityArgumentType.getPlayer(context, "target"), context.getSource());
			return 0;
		});

		randomTeleport.then(target);
		dispatcher.register(randomTeleport);
		dispatcher.register(CommandManager.literal("rtp").redirect(randomTeleport.build()));
	}

	private static void teleportRandomly(ServerPlayerEntity player, ServerCommandSource source) {
		KiloPlayer kiloPlayer = KiloPlayerManager.getPlayerData(player.getUuid());
		if (kiloPlayer.rtpLeft == 0
				|| Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.rtp.ignorelimit", 4)) {
			player.sendMessage(LangText.get(true, "command.randomteleport.runout"));
		} else {
			Random random = new Random();
			int randomX = random.nextInt(19000) + 1000; // 1000 - 20000
			int randomZ = random.nextInt(19000) + 1000; // 1000 - 20000

			// Negative coords as well
			if (random.nextInt(2) == 0) {
				randomX *= -1;
			}

			if (random.nextInt(2) == 0) {
				randomZ *= -1;
			}

			if (player.world.getBiome(new BlockPos(randomX, 65, randomZ)).getCategory() == Category.OCEAN) {
				teleportRandomly(player);
			} else {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 30, 255, false, true, true));
				player.teleport(randomX, 255, randomZ);
				kiloPlayer.rtpLeft -= 1;

				player.sendMessage(LangText.getFormatter(true, "command.randomteleport.success",
						"X: " + randomX + ", Z: " + randomZ, kiloPlayer.rtpLeft));
			}
		}
	}
}
