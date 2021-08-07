package org.kilocraft.essentials.mixin.patch;

import net.minecraft.item.FilledMapItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FilledMapItem.class)
public class FilledMapItemMixinPatch {

    @Redirect(method = "updateColors", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getWorldChunk(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/chunk/WorldChunk;"))
    public WorldChunk getChunkIfLoaded(World world, BlockPos pos) {
        //Maps shouldn't load chunks
        return world.getChunkManager().getWorldChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    @Redirect(method = "updateColors", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;isEmpty()Z"))
    public boolean validateChunkNotNull(WorldChunk worldChunk) {
        //worldChunk may be null, because of the above redirect
        if (worldChunk == null) {
            return true;
        } else {
            return worldChunk.isEmpty();
        }
    }

}
