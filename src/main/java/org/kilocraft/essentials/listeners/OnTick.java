package org.kilocraft.essentials.listeners;

import net.fabricmc.loader.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.ServerTickEvent;
import org.kilocraft.essentials.user.ServerUserManager;
import java.util.List;

public class OnTick implements EventHandler<ServerTickEvent> {

	@Override
	public void handle(ServerTickEvent event) {
		((ServerUserManager) KiloServer.getServer().getUserManager()).onTick();
		processParticles(event);
	}

	private void processParticles(ServerTickEvent event) {
		ServerWorld world = event.getServer().getWorld(DimensionType.OVERWORLD);
		List<ServerPlayerEntity> players = world.getPlayers();

		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).isSpectator() == true/* || FabricLoader.INSTANCE.isModLoaded("vanish")*/) {
				return;
			}
			
			int particle = KiloServer.getServer().getUserManager().getOnline(players.get(i).getUuid()).getDisplayParticleId();
			if (particle != 0) {
				double x = players.get(i).getX();
				double y = players.get(i).getY();
				double z = players.get(i).getZ();
				
				for (int j = 0; j < players.size(); j++) {
					ServerPlayerEntity player = players.get(j);
					
					if (particle == 1) {
						 //Player, visable to only you, position (X, Y, Z), speed, delta (X, Y, Z), count
						world.spawnParticles(player, ParticleTypes.LAVA, true, x, y, z,
								0, 0.25f, 0, 0.25f, 1);
					} else if (particle == 2) {
						world.spawnParticles(player,
								new BlockStateParticleEffect(ParticleTypes.BLOCK,
										Blocks.WHITE_STAINED_GLASS.getDefaultState()),
								false, x, y, z, 2, 0.25f, 0,
								0.25f, 2);
						world.spawnParticles(player,
								new BlockStateParticleEffect(ParticleTypes.BLOCK,
										Blocks.YELLOW_STAINED_GLASS.getDefaultState()),
								false, x, y, z, 2, 0.25f, 0,
								0.25f, 2);
						world.spawnParticles(player,
								new BlockStateParticleEffect(ParticleTypes.BLOCK,
										Blocks.ORANGE_STAINED_GLASS.getDefaultState()),
								false, x, y, z, 2, 0.25f, 0,
								0.25f, 2);
						world.spawnParticles(player,
								new BlockStateParticleEffect(ParticleTypes.BLOCK,
										Blocks.RED_STAINED_GLASS.getDefaultState()),
								false, x, y, z, 2, 0.25f, 0,
								0.25f, 2);
					} else if (particle == 3) {
						world.spawnParticles(player, ParticleTypes.CLOUD, true, x,
								y + 3.5f, z, 10, 0.6f, 0.15f, 0.6f, 0);
						world.spawnParticles(player, ParticleTypes.RAIN, true, x,
								y + 3.55f, z, 3, 0.4f, 0f, 0.4f, 2);
					}
				}
			}
		}
	}

}
