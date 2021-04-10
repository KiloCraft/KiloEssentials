package org.kilocraft.essentials.mixin.patch;

import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.biome.source.BiomeArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkDataS2CPacket.class)
public class ChunkDataS2CPacketMixin {

    @Redirect(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/source/BiomeArray;toIntArray()[I"))
    public int[] fixInvalidBiomeId(BiomeArray biomeArray) {
        int[] result = biomeArray.toIntArray();
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i] == -1 ? 0 : result[i];
        }
        return result;
    }

}
