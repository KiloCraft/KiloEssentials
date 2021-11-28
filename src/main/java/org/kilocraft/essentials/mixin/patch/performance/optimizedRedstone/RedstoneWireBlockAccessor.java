package org.kilocraft.essentials.mixin.patch.performance.optimizedRedstone;

import net.minecraft.world.level.block.RedStoneWireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RedStoneWireBlock.class)
public interface RedstoneWireBlockAccessor {

    @Accessor("shouldSignal")
    void setShouldSignal(boolean shouldSignal);

}
