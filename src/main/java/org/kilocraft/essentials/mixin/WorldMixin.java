package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.kilocraft.essentials.util.DataTracker;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable {

    @Redirect(method = "tickEntity", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    public <T> void shouldTickEntity(Consumer<T> consumer, T t) {
        if (!ServerSettings.TICK_ENTITIES.getValue()) return;
        int tickDistance = ServerSettings.TICK_DISTANCE.getValue();
        if (t instanceof Entity && tickDistance != -1) {
            if (t instanceof VillagerEntity && !ServerSettings.TICK_VILLAGERS.getValue()) return;
            Entity entity = (Entity) t;
            Entity player = entity.world.getClosestPlayer(entity, -1.0D);
            if (player != null) {
                if (entity.getChunkPos().getChebyshevDistance(player.getChunkPos()) > tickDistance) {
                    DataTracker.add(DataTracker.cTickedEntities);
                    return;
                }
            }
        }
        DataTracker.add(DataTracker.tickedEntities);
        consumer.accept(t);
    }

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"))
    public void shouldTickBlockEntity(BlockEntityTickInvoker blockEntityTickInvoker) {
        ChunkPos chunkPos = new ChunkPos(blockEntityTickInvoker.getPos());
        int tickDistance = ServerSettings.TICK_DISTANCE.getValue();
        if (tickDistance != -1) {
            Entity player = this.getClosestPlayer(chunkPos.getStartX() + 8, 128, chunkPos.getStartZ() + 8, -1.0D, false);
            if (player != null) {
                if (chunkPos.getChebyshevDistance(player.getChunkPos()) > tickDistance) {
                    DataTracker.add(DataTracker.cTickedBlockEntities);
                    return;
                }
            }
        }
        DataTracker.add(DataTracker.tickedBlockEntities);
        blockEntityTickInvoker.tick();
    }

}
