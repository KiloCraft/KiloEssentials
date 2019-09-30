package org.kilocraft.essentials.api.world.worldimpl;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.world.Block;
import org.kilocraft.essentials.api.world.World;

import java.util.Optional;

public class WorldImpl implements World {
    private net.minecraft.world.World serverWorld;

    public WorldImpl(net.minecraft.world.World world) {
        this.serverWorld = world;
    }

    @Override
    public void setBlockAt(Vec3d pos, Blocks block) {
        Optional<net.minecraft.block.Block> b = InternalBlockConverter.convertBlock(block);

        // We might not want to silently ignore this error
        b.ifPresent(value -> serverWorld.setBlockState(new BlockPos(pos.x, pos.y, pos.z), value.getDefaultState()));
    }

    @Override
    public Block getBlockAt(Vec3d pos) {
        return new BlockImpl(this, serverWorld.getBlockState(new BlockPos(pos.x, pos.y, pos.z)), pos);
    }

    @Override
    public String getName() {
        return null;
    }
}
