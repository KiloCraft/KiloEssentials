package org.kilocraft.essentials.api.World;

import jdk.nashorn.internal.ir.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.Vec3d;

public interface World {
    void setBlockAt(Vec3d pos, Blocks block);

    Block getBlockAt(Vec3d pos);

    String getName();
}
