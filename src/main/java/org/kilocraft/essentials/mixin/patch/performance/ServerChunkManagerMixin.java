package org.kilocraft.essentials.mixin.patch.performance;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.util.TickManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {

    /*
     * Copied from:
     * https://github.com/Wesley1808/ServerCore-Fabric/blob/1.17.1/src/main/java/org/provim/servercore/mixin/performance/ServerChunkManagerMixin.java
     * */

    @Unique
    private final ObjectLinkedOpenHashSet<ServerChunkManager.ChunkWithHolder> active = new ObjectLinkedOpenHashSet<>();
    @Shadow
    @Final
    ServerWorld world;

    // Avoid massive array allocations.
    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayListWithCapacity(I)Ljava/util/ArrayList;"))
    private ArrayList<?> noList(int initialArraySize) {
        return null;
    }

    // Cancel any operations performed on the list.
    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Ljava/util/Collections;shuffle(Ljava/util/List;)V"))
    private void cancelShuffle(List<?> list) {
    }

    // Replaces chunk filtering with our own implementation.
    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;", ordinal = 0))
    private Iterator<ChunkHolder> updateFilteredChunks(Iterable<ChunkHolder> iterable) {
        this.updateActiveChunks(iterable);
        return Collections.emptyIterator();
    }

    // Only iterate through active chunks.
    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0))
    private Iterator<?> onlyTickActiveChunks(List<?> list) {
        return this.active.iterator();
    }

    // Only flush active chunks.
    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0))
    private void flushActiveChunks(List<?> list, Consumer<? super ServerChunkManager.ChunkWithHolder> action) {
        this.active.forEach(action);
    }

    private void updateActiveChunks(Iterable<ChunkHolder> holders) {
        // Updates active chunks once a second.
        if (KiloEssentials.getMinecraftServer().getTicks() % 20 == 0) {
            // Add active chunks
            for (ChunkHolder holder : holders) {
                final WorldChunk chunk = holder.getTickingFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).left().orElse(null);
                if (chunk != null) {
                    if (TickManager.shouldTick(holder.getPos(), this.world)) {
                        this.active.add(new ServerChunkManager.ChunkWithHolder(chunk, holder));
                    } else {
                        // Sends clients block updates from inactive chunks.
                        holder.flushUpdates(chunk);
                    }
                }
            }
            // Remove inactive chunks
            this.active.removeIf(lv -> lv.holder().getTickingFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).left().isEmpty() || !TickManager.shouldTick(lv.holder().getPos(), this.world));
        }
    }

}