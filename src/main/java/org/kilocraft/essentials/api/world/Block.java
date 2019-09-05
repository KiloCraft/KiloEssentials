package org.kilocraft.essentials.api.world;


import net.minecraft.block.BlockState;
import org.kilocraft.essentials.api.math.Vec3d;

public interface Block {

    Vec3d getLocation();

    World getWorld();

    BlockState getDefaultState();
}
