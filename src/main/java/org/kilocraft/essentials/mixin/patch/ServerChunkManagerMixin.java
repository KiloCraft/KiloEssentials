package org.kilocraft.essentials.mixin.patch;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {

    /*
    * Copied from:
    * https://github.com/Wesley1808/ServerCore-Fabric/blob/1.17.1/src/main/java/org/provim/servercore/mixin/performance/ServerChunkManagerMixin.java
    * */

    /**
     * Stops minecraft from initializing a massive list of ChunkHolders, as we already get those directly from TACS.
     * The drawback is that it will also stop the order of ticking chunks from being shuffled.
     * This is an unnoticeable change besides maybe specific complex redstone contraptions that can detect it.
     */

    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;entryIterator()Ljava/lang/Iterable;"))
    private Iterable<ChunkHolder> emptyList(ThreadedAnvilChunkStorage threadedAnvilChunkStorage) {
        return Collections.emptyList();
    }

    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Ljava/util/Collections;shuffle(Ljava/util/List;)V"))
    private void cancelShuffle(List<?> list) {
    }

    // Chunk flushing is already done in the iteration above, because these iterations are expensive.
    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 1))
    private <T> void cancelChunkFlushing(List<ChunkHolder> list, Consumer<? super T> action) {
    }
}