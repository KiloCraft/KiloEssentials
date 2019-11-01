package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
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
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.ThreadManager;
import org.kilocraft.essentials.craft.threaded.ThreadedRandomTeleporter;
import org.kilocraft.essentials.craft.user.User;
import org.kilocraft.essentials.craft.user.UserManager;

import java.util.Random;

public class RandomTeleportCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		KiloCommands.getCommandPermission("rtp");
		KiloCommands.getCommandPermission("rtp.self");
		KiloCommands.getCommandPermission("rtp.others");
		KiloCommands.getCommandPermission("rtp.ignorelimit");
		LiteralCommandNode<ServerCommandSource> randomTeleport = CommandManager.literal("randomteleport")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("rtp.self"), 2))
				.executes(context -> {
					teleportRandomly(context.getSource().getPlayer(), context.getSource());
					return 0;
				}).build();

		ArgumentCommandNode<ServerCommandSource, EntitySelector> target = CommandManager
				.argument("player", EntityArgumentType.player())
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("rtp.others"), 2))
				.executes(context -> execute(EntityArgumentType.getPlayer(context, "player"), context.getSource())).build();

		randomTeleport.addChild(target);
		dispatcher.getRoot().addChild(randomTeleport);
		dispatcher.getRoot().addChild(CommandManager.literal("rtp").requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("rtp.self"), 2))
				.executes(context -> execute(context.getSource().getPlayer(), context.getSource())).redirect(randomTeleport).build());
	}

	private static int execute(ServerPlayerEntity player, ServerCommandSource source) {
		ThreadManager thread = new ThreadManager(new ThreadedRandomTeleporter(player, source));
		thread.start();

		return 1;
	}

	public static void teleportRandomly(ServerPlayerEntity player, ServerCommandSource source) {
		User user = UserManager.getUser(player.getUuid());
		if (user.getRandomTeleportsLeft() == 0 || !Thimble.hasPermissionOrOp(source, KiloCommands.getCommandPermission("rtp.ignorelimit"), 2)) {
			player.sendMessage(LangText.get(true, "command.randomteleport.runout"));
		} else {
			Random random = new Random();
			int randomX = random.nextInt(14000) + 1000; // 1000 - 15000
			int randomZ = random.nextInt(14000) + 1000; // 1000 - 15000

			// Negative coords as well
			if (random.nextInt(2) == 0) {
				randomX *= -1;
			}

			if (random.nextInt(2) == 0) {
				randomZ *= -1;
			}

			if (player.world.getBiome(new BlockPos(randomX, 65, randomZ)).getCategory() == Category.OCEAN) {
				teleportRandomly(player, source);
			} else {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 500, 255, false, false, false));
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 500, 255, false, false, false));
				player.teleport(randomX, 255, randomZ);
				user.setRandomTeleportsLeft(user.getRandomTeleportsLeft() - 1);

				player.sendMessage(LangText.getFormatter(true, "command.randomteleport.success",
						"X: " + randomX + ", Z: " + randomZ, user.getRandomTeleportsLeft()));
			}
		}
	}
}
