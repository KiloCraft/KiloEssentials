package org.kilocraft.essentials.patch.optimizedSpawning;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import org.kilocraft.essentials.util.registry.RegistryKeyID;
import org.kilocraft.essentials.util.settings.ServerSettings;

public class SpawnUtil {

    public static float getMobCapMultiplier(ServerWorld world, SpawnGroup group) {
        return getMobCapMultiplier(world, group.ordinal() + 1);
    }

    public static float getMobCapMultiplier(ServerWorld world, int index) {
        return ServerSettings.mobcap[((RegistryKeyID) world.getRegistryKey()).getID()][index];
    }

    public static int getPersonalMobCap(ServerWorld world, SpawnGroup group) {
        return (int) (group.getCapacity() * ServerSettings.tick_utils_global_mobcap * getMobCapMultiplier(world, 0) * getMobCapMultiplier(world, group));
    }

    public static int getGlobalMobCap(SpawnHelper.Info info, ServerWorld world, SpawnGroup group) {
        return getPersonalMobCap(world, group) * info.getSpawningChunkCount() / SpawnHelper.CHUNK_AREA;
    }

}
