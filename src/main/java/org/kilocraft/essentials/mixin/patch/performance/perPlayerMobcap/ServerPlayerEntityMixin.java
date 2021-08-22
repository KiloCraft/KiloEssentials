package org.kilocraft.essentials.mixin.patch.performance.perPlayerMobcap;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.patch.perPlayerMobSpawn.PooledHashSets;
import org.kilocraft.essentials.patch.perPlayerMobSpawn.ServerPlayerEntityInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityInterface {

    int[] mobcounts = new int[SpawnGroup.values().length];

    PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayerEntity> cachedSingleMobDistanceMap;

    @Override
    public PooledHashSets.PooledObjectLinkedOpenHashSet<ServerPlayerEntity> getCachedSingleMobDistanceMap() {
        return cachedSingleMobDistanceMap;
    }

    @Override
    public int[] getMobCounts() {
        return mobcounts;
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(MinecraftServer minecraftServer, ServerWorld serverWorld, GameProfile gameProfile, CallbackInfo ci) {
        cachedSingleMobDistanceMap = new PooledHashSets.PooledObjectLinkedOpenHashSet<>((ServerPlayerEntity) (Object) this);
    }

}