package org.kilocraft.essentials.extensions.warps.playerwarps;

import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.extensions.warps.Warp;

import java.util.UUID;

public class PlayerWarp extends Warp {
    private UUID owner;

    public PlayerWarp(String name, Location location, UUID owner) {
        super(name, location);
        this.owner = owner;
    }

    public UUID getOwner() {
        return this.owner;
    }

}
