package org.kilocraft.essentials.mixin.patch.bugfix;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SurfaceBuilder.class)
public abstract class SurfaceBuilderMixin {


    /*
    * Bugfix for MC-239854
    * */
    @Redirect(
            method = "buildSurface",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/surfacebuilder/MaterialRules$BlockStateRule;tryApply(III)Lnet/minecraft/block/BlockState;"
            )
    )
    private BlockState dontReplaceBedrock(MaterialRules.BlockStateRule instance, int x, int y, int z, BiomeAccess biomeAccess, Registry<Biome> biomeRegistry, boolean useLegacyRandom, HeightContext context, Chunk chunk, ChunkNoiseSampler chunkNoiseSampler, MaterialRules.MaterialRule surfaceRule) {
        final BlockState oldState = chunk.getBlockState(new BlockPos(x, y, z));
        return (oldState != null && oldState.getBlock() == Blocks.BEDROCK) ? null : instance.tryApply(x, y, z);
    }
}
