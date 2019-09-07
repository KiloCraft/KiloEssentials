package org.kilocraft.essentials.api.world.worldimpl;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.world.Block;
import org.kilocraft.essentials.api.world.World;

public class BlockImpl implements Block {

    private final BlockState block;
    private final Vec3d pos;
    private final World world;

    public BlockImpl(World world, BlockState block, Vec3d pos) {
        this.world = world;
        this.block = block;
        this.pos = pos;
    }

    @Override
    public Vec3d getLocation() {
        return pos;
    }

    @Override
    public World getWorld() {
        return world;
    }
}