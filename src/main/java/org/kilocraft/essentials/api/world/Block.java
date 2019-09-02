package org.kilocraft.essentials.api.world;

import net.minecraft.client.render.Vec3d;
import net.minecraft.world.World;

public interface Block {

    Vec3d getLocation();

    World getWorld();
}
