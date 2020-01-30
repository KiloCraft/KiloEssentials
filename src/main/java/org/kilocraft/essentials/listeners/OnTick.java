package org.kilocraft.essentials.listeners;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.ServerTickEvent;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.ServerUserManager;

public class OnTick implements EventHandler<ServerTickEvent> {

	// Spread particle processing over two frames to increase performance
	boolean processFirstHalf = true;

	@Override
	public void handle(ServerTickEvent event) {
		((ServerUserManager) KiloServer.getServer().getUserManager()).onTick();

		for (ServerPlayerEntity player : KiloServer.getServer().getPlayerManager().getPlayerList()) {
			processDimension(player);
		}

		if (processFirstHalf){
			for (int i = 0; i < KiloServer.getServer().getPlayerManager().getPlayerList().size() / 2; i++) {
				ServerPlayerEntity player = KiloServer.getServer().getPlayerManager().getPlayerList().get(i);
				if (KiloServer.getServer().getOnlineUser(player).getDisplayParticleId() != 0) {
					processParticles(player);
				}
			}
		} else {
			for (int i = KiloServer.getServer().getPlayerManager().getPlayerList().size() / 2; i < KiloServer.getServer().getPlayerManager().getPlayerList().size(); i++) {
				ServerPlayerEntity player = KiloServer.getServer().getPlayerManager().getPlayerList().get(i);
				if (KiloServer.getServer().getOnlineUser(player).getDisplayParticleId() != 0) {
					processParticles(player);
				}
			}
		}

		processFirstHalf = !processFirstHalf;
	}

	private void processDimension(ServerPlayerEntity player) {
		boolean allowNether = KiloConfig.getProvider().getMain().getBooleanSafely("server.world.allow_nether", false);
		boolean allowTheEnd = KiloConfig.getProvider().getMain().getBooleanSafely("server.world.allow_the_end", false);
		boolean kickFromDim = KiloConfig.getProvider().getMain().getBooleanSafely("server.also_kick_from_dim", false);

		if (kickFromDim &&
				(!allowNether && player.getEntityWorld().getDimension().getType().equals(DimensionType.THE_NETHER)) ||
				!allowTheEnd && player.getEntityWorld().getDimension().getType().equals(DimensionType.THE_END))
			player.requestRespawn();

	}

	private void processParticles(ServerPlayerEntity player) {
		ServerWorld world = player.getServer().getWorld(DimensionType.OVERWORLD);

		int particle = KiloServer.getServer().getUserManager().getOnline(player).getDisplayParticleId();
		if (particle != 0) {
			double x = player.getX();
			double y = player.getY();
			double z = player.getZ();

			if (particle == 1) {
				//Player, particle, longer visibility distance, position (X, Y, Z), count, delta (X, Y, Z), speed
				world.spawnParticles(player, ParticleTypes.LAVA, false, x, y, z,
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
				world.spawnParticles(player, ParticleTypes.CLOUD, false, x,
						y + 3.5f, z, 10, 0.6f, 0.15f, 0.6f, 0);
				world.spawnParticles(player, ParticleTypes.RAIN, false, x,
						y + 3.55f, z, 3, 0.4f, 0f, 0.4f, 2);
			} else if (particle == 4) {
				world.spawnParticles(player, ParticleTypes.DRAGON_BREATH, false, x,
						y, z, 10, 0.5, 0f, 0.5f, 0);
			} else if (particle == 5) {
				world.spawnParticles(player, ParticleTypes.HAPPY_VILLAGER, false, x,
						y, z, 3, 0.4, 0f, 0.4f, 0);
			}
		}
	}
}
