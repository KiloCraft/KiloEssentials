package org.kilocraft.essentials.patch;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.kilocraft.essentials.util.registry.IResourceKey;
import org.kilocraft.essentials.util.settings.ServerSettings;

public class SpawnUtil {

    public static float getMobCapMultiplier(ServerLevel world, MobCategory group) {
        return getMobCapMultiplier(world, group.ordinal() + 1);
    }

    public static float getMobCapMultiplier(ServerLevel world, int index) {
        return ServerSettings.mobcap[((IResourceKey) world.dimension()).getID()][index];
    }

    public static int getPersonalMobCap(ServerLevel world, MobCategory group) {
        return (int) (group.getMaxInstancesPerChunk() * getMobCapMultiplier(world, 0) * getMobCapMultiplier(world, group));
    }

    public static int getGlobalMobCap(NaturalSpawner.SpawnState info, ServerLevel world, MobCategory group) {
        return getPersonalMobCap(world, group) * info.getSpawnableChunkCount() / NaturalSpawner.MAGIC_NUMBER;
    }

}
