package org.kilocraft.essentials.api.entity;

import org.kilocraft.essentials.api.math.Vec3d;
import org.kilocraft.essentials.api.world.World;

import java.util.UUID;

public interface Entity {

    /**
     * Get entity name
     *
     * @return Entity name
     */
    String getName();

    /**
     * Get entity's custom name
     *
     * @return Custom name
     */
    String getCustomName();

    /**
     * Set entity's custom name
     *
     * @param name New custom name
     */
    void setCustomName(String name);

    /**
     * Get entity's position
     *
     * @return Entity's position
     */
    Vec3d getPos();

    /**
     * Get entity's UUID
     *
     * @return Entity's UUID
     */
    UUID getUUID();

    /**
     * Get the world the entity is in
     *
     * @return the world the entity is in
     */
    World getWorld();

    /**
     * Teleport the entity
     *
     * @param pos Position to teleport the entity to
     */
    void teleport(Vec3d pos);

}
