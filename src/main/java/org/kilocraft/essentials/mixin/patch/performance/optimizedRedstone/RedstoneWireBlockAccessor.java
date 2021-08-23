package org.kilocraft.essentials.mixin.patch.performance.optimizedRedstone;

import net.minecraft.block.RedstoneWireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RedstoneWireBlock.class)
public interface RedstoneWireBlockAccessor {

    @Accessor("wiresGivePower")
    void setWireGivesPower(boolean wiresGivePower);

}
