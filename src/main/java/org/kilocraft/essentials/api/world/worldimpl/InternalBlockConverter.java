package org.kilocraft.essentials.api.world.worldimpl;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.kilocraft.essentials.api.Mod;

import java.util.Optional;

class InternalBlockConverter {

    static Optional<Block> convertBlock(Blocks block) {
        try {
            return Optional.ofNullable((Block) Blocks.class.getDeclaredField(block.toString()).get(null));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Mod.getLogger.error("Error getting block for " + block.toString()+ "!");
            e.printStackTrace();
        }

        return Optional.empty();
    }
}
