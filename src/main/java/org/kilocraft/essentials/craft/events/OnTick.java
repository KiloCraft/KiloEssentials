package org.kilocraft.essentials.craft.events;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.serverEvents.ServerEvent$OnTick;
import org.kilocraft.essentials.craft.player.KiloPlayerManager;

import net.minecraft.block.Blocks;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionType;

public class OnTick implements EventHandler<ServerEvent$OnTick> {

	@Override
	public void handle(ServerEvent$OnTick event) {
		ServerWorld world = event.getServer().getWorld(DimensionType.OVERWORLD);
		ServerPlayerEntity[] players = (ServerPlayerEntity[]) world.getPlayers().toArray();
		System.out.println("TEST");
		System.out.println(players.length);
		for (int i = 0; i < players.length; i++) {
			String particle = KiloPlayerManager.getPlayerData(players[i].getUuid()).particle;
			System.out.println(particle);
			if (particle != "none") {
				if (particle == "flames") {
					// Player, visable to all, position (X, Y, Z), count, delta (X, Y, Z), speed
					world.spawnParticles(players[i], ParticleTypes.LAVA, true, players[i].getX(), players[i].getY(),
							players[i].getZ(), 3, 0.25f, 0, 0.25f, 1);
				} else if (particle == "glass") {
					world.spawnParticles(players[i],
							new BlockStateParticleEffect(ParticleTypes.BLOCK,
									Blocks.WHITE_STAINED_GLASS.getDefaultState()),
							true, players[i].getX(), players[i].getY(), players[i].getZ(), 3, 0.25f, 0, 0.25f, 1);
					world.spawnParticles(players[i],
							new BlockStateParticleEffect(ParticleTypes.BLOCK,
									Blocks.YELLOW_STAINED_GLASS.getDefaultState()),
							true, players[i].getX(), players[i].getY(), players[i].getZ(), 3, 0.25f, 0, 0.25f, 1);
					world.spawnParticles(players[i],
							new BlockStateParticleEffect(ParticleTypes.BLOCK,
									Blocks.ORANGE_STAINED_GLASS.getDefaultState()),
							true, players[i].getX(), players[i].getY(), players[i].getZ(), 3, 0.25f, 0, 0.25f, 1);
					world.spawnParticles(players[i],
							new BlockStateParticleEffect(ParticleTypes.BLOCK,
									Blocks.RED_STAINED_GLASS.getDefaultState()),
							true, players[i].getX(), players[i].getY(), players[i].getZ(), 3, 0.25f, 0, 0.25f, 1);
				} else if (particle == "rain") {
					world.spawnParticles(players[i], ParticleTypes.CLOUD, true, players[i].getX(), players[i].getY() + 2.5f,
							players[i].getZ(), 3, 1, 0.2f, 1, 1);
					world.spawnParticles(players[i], ParticleTypes.RAIN, true, players[i].getX(), players[i].getY() + 2.6f,
							players[i].getZ(), 3, 0.8f, 0f, 0.8f, 1);
				}
			}
		}
	}

}
