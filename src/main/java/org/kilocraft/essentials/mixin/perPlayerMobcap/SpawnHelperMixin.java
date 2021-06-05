package org.kilocraft.essentials.mixin.perPlayerMobcap;

import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.mixin.accessor.SpawnHelperAccessor;
import org.kilocraft.essentials.mixin.accessor.SpawnHelperInfoAccessor;
import org.kilocraft.essentials.patch.perPlayerMobSpawn.ThreadedAnvilChunkStorageInterface;
import org.kilocraft.essentials.util.registry.RegistryKeyID;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {

    @Shadow
    @Final
    private static SpawnGroup[] SPAWNABLE_GROUPS;

    @Shadow
    protected static BlockPos getSpawnPos(World world, WorldChunk worldChunk) {
        return null;
    }

    @Shadow
    protected static boolean isAcceptableSpawnPosition(ServerWorld serverWorld, Chunk chunk, BlockPos.Mutable mutable, double d) {
        return false;
    }

    @Shadow
    protected static Optional<SpawnSettings.SpawnEntry> pickRandomSpawnEntry(ServerWorld serverWorld, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, Random random, BlockPos blockPos) {
        return null;
    }

    @Shadow
    protected static MobEntity createMob(ServerWorld serverWorld, EntityType<?> entityType) {
        return null;
    }

    @Shadow
    protected static boolean isValidSpawn(ServerWorld serverWorld, MobEntity mobEntity, double d) {
        return false;
    }

    /**
     * @author Drex
     */
    @Overwrite
    public static void spawn(ServerWorld serverWorld, WorldChunk chunk, SpawnHelper.Info info, boolean flag, boolean flag1, boolean flag2) {
        serverWorld.getProfiler().push("spawner");
        SpawnGroup[] spawnGroups = SPAWNABLE_GROUPS;
        for (SpawnGroup spawnGroup : spawnGroups) {
            int currEntityCount = info.getGroupToCount().getInt(spawnGroup);
            float multiplier = ServerSettings.mobcap[((RegistryKeyID) serverWorld.getRegistryKey()).getID()][0] *
                    ServerSettings.mobcap[((RegistryKeyID) serverWorld.getRegistryKey()).getID()][spawnGroup.ordinal()];
            int k1 = (int) (spawnGroup.getCapacity() * ((SpawnHelperInfoAccessor) info).getSpawnChunkCount() / SpawnHelperAccessor.getChunkArea() * multiplier);
            int difference = k1 - currEntityCount;

            if (ServerSettings.perPlayerMobcap) {
                int minDiff = Integer.MAX_VALUE;
                for (ServerPlayerEntity player : ((ThreadedAnvilChunkStorageInterface) serverWorld.getChunkManager().threadedAnvilChunkStorage).getMobDistanceMap().getPlayersInRange(chunk.getPos())) {
                    int ticksPlayed = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
                    if (spawnGroup.isPeaceful() || ticksPlayed <= KiloConfig.main().startHelp * 1200) {
                        minDiff = (int) Math.min((spawnGroup.getCapacity() * multiplier) - ((ThreadedAnvilChunkStorageInterface) serverWorld.getChunkManager().threadedAnvilChunkStorage).getMobCountNear(player, spawnGroup), minDiff);
                    }
                }
                difference = (minDiff == Integer.MAX_VALUE) ? 0 : minDiff;
            }

            if ((flag || !spawnGroup.isPeaceful()) && (flag1 || spawnGroup.isPeaceful()) && (flag2 || !spawnGroup.isRare()) && difference > 0) {
                spawnEntitiesInChunk(spawnGroup, serverWorld, chunk, ((SpawnHelperInfoAccessor) info)::test, ((SpawnHelperInfoAccessor) info)::run,
                        difference, ServerSettings.perPlayerMobcap ? ((ThreadedAnvilChunkStorageInterface) serverWorld.getChunkManager().threadedAnvilChunkStorage)::updatePlayerMobTypeMap : null);
            }
        }
        serverWorld.getProfiler().pop();
    }


    private static int spawnEntitiesInChunk(SpawnGroup spawnGroup, ServerWorld serverWorld, WorldChunk worldChunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner, int maxSpawns, Consumer<Entity> trackEntity) {
        BlockPos blockPos = getSpawnPos(serverWorld, worldChunk);
        if (blockPos.getY() >= serverWorld.getBottomY() + 1) {
            return spawnMobsInternal(spawnGroup, serverWorld, worldChunk, blockPos, checker, runner, maxSpawns, trackEntity);
        }
        return 0;
    }

    private static int spawnMobsInternal(SpawnGroup spawnGroup, ServerWorld serverWorld, Chunk chunk, BlockPos blockPos, SpawnHelper.Checker checker, SpawnHelper.Runner runner, int maxSpawns, Consumer<Entity> trackEntity) {
        StructureAccessor structureAccessor = serverWorld.getStructureAccessor();
        ChunkGenerator chunkGenerator = serverWorld.getChunkManager().getChunkGenerator();
        int i = blockPos.getY();
        int spawns = 0;
        BlockState blockState = chunk.getBlockState(blockPos);
        if (blockState != null && !blockState.isSolidBlock(chunk, blockPos)) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (int k = 0; k < 3; ++k) {
                int l = blockPos.getX();
                int m = blockPos.getZ();
                SpawnSettings.SpawnEntry spawnEntry;
                EntityData entityData = null;
                int o = MathHelper.ceil(serverWorld.random.nextFloat() * 4.0F);
                int p = 0;

                for (int q = 0; q < o; ++q) {
                    l += serverWorld.random.nextInt(6) - serverWorld.random.nextInt(6);
                    m += serverWorld.random.nextInt(6) - serverWorld.random.nextInt(6);
                    mutable.set(l, i, m);
                    double d = (double) l + 0.5D;
                    double e = (double) m + 0.5D;
                    PlayerEntity playerEntity = serverWorld.getClosestPlayer(d, i, e, -1.0D, false);
                    if (playerEntity != null) {
                        double f = playerEntity.squaredDistanceTo(d, i, e);
                        if (isAcceptableSpawnPosition(serverWorld, chunk, mutable, f) && serverWorld.isChunkLoaded(mutable)) {
                            Optional<SpawnSettings.SpawnEntry> optional = pickRandomSpawnEntry(serverWorld, structureAccessor, chunkGenerator, spawnGroup, serverWorld.random, mutable);
                            if (!optional.isPresent()) {
                                break;
                            }

                            spawnEntry = optional.get();
                            o = spawnEntry.minGroupSize + serverWorld.random.nextInt(1 + spawnEntry.maxGroupSize - spawnEntry.minGroupSize);

                            if (SpawnHelperAccessor.canSpawn(serverWorld, spawnGroup, structureAccessor, chunkGenerator, spawnEntry, mutable, f) && checker.test(spawnEntry.type, mutable, chunk)) {
                                MobEntity mobEntity = createMob(serverWorld, spawnEntry.type);
                                if (mobEntity == null) {
                                    return spawns;
                                }

                                mobEntity.refreshPositionAndAngles(d, i, e, serverWorld.random.nextFloat() * 360.0F, 0.0F);
                                if (isValidSpawn(serverWorld, mobEntity, f)) {
                                    entityData = mobEntity.initialize(serverWorld, serverWorld.getLocalDifficulty(mobEntity.getBlockPos()), SpawnReason.NATURAL, entityData, null);
                                    ++spawns;
                                    ++p;
                                    serverWorld.spawnEntityAndPassengers(mobEntity);
                                    runner.run(mobEntity, chunk);
                                    if (trackEntity != null) {
                                        trackEntity.accept(mobEntity);
                                    }
                                    if (spawns >= mobEntity.getLimitPerChunk() || spawns >= maxSpawns) {
                                        return spawns;
                                    }

                                    if (mobEntity.spawnsTooManyForEachTry(p)) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        return spawns;
    }


}
