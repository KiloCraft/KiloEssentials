package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.kilocraft.essentials.util.math.DataTracker;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable {

    @Redirect(method = "tickEntity", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    public <T> void shouldTickEntity(Consumer<T> consumer, T t) {
        if (!ServerSettings.entityTickCache[0]) return;
        int tickDistance = ServerSettings.getInt("tick.distance");
        if (t instanceof Entity ) {
            Entity entity = (Entity) t;
            if (!ServerSettings.entityTickCache[Registry.ENTITY_TYPE.getRawId(entity.getType()) + 1]) return;
            if (tickDistance != -1){
                Entity player = entity.world.getClosestPlayer(entity, -1.0D);
                if (player != null) {
                    if (entity.getChunkPos().getChebyshevDistance(player.getChunkPos()) > tickDistance) {
                        DataTracker.cTickedEntities.track();
                        return;
                    }
                }
            }
        }
        DataTracker.tickedEntities.track();
        consumer.accept(t);
    }

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"))
    public void shouldTickBlockEntity(BlockEntityTickInvoker blockEntityTickInvoker) {
        int tickDistance = ServerSettings.getInt("tick.distance");
        if (tickDistance != -1) {
            ChunkPos chunkPos = new ChunkPos(blockEntityTickInvoker.getPos());
            Entity player = this.getClosestPlayer(chunkPos.getStartX() + 8, 128, chunkPos.getStartZ() + 8, -1.0D, false);
            if (player != null) {
                if (chunkPos.getChebyshevDistance(player.getChunkPos()) > tickDistance) {
                    DataTracker.cTickedBlockEntities.track();
                    return;
                }
            }
        }
        DataTracker.tickedBlockEntities.track();
        blockEntityTickInvoker.tick();
    }

}
