package org.kilocraft.essentials.mixin.patch.bugfix;

import net.minecraft.class_6752;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ReadOnlyChunk.class)
public abstract class ReadOnlyChunkMixin extends ProtoChunk {

    /*
    * Bugfix for MC-237986
    * */

    @Shadow
    @Final
    private WorldChunk wrapped;

    public ReadOnlyChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> registry, @Nullable class_6752 arg) {
        super(chunkPos, upgradeData, heightLimitView, registry, arg);
    }

    @Override
    public boolean hasStructureReferences() {
        return this.wrapped.hasStructureReferences();
    }
}
