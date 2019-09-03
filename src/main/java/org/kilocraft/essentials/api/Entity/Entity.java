package org.kilocraft.essentials.api.Entity;

import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.World.World;

import java.util.UUID;

public interface Entity {
    String getName();

    String getCustomName();

    void setCustomName(String customName);

    Vec3d getPos();

    UUID getUniqeId();

    World getWorld();

    void teleport(Vec3d pos);
}
