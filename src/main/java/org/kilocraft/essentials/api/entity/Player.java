package org.kilocraft.essentials.api.entity;

import net.minecraft.network.MessageType;
import org.kilocraft.essentials.api.world.Block;

import java.util.Optional;

public interface Player extends Entity {

    /**
     * Gets the block the player is looking at
     *
     * @param distance     How far should we check before giving up
     * @param returnFluids Should we return fluids?
     * @return Block, or empty if we couldn't find anything or we found an entity
     */
    Optional<Block> getTargetBlock(double distance, boolean returnFluids);

    /**
     * Send a chat message to this player
     *
     * @param message Message to send
     */
    void send(String message);

    /**
     * Send a message to this player
     *
     * @param message Message to send
     * @param type    Type of the sent message
     */
    void send(String message, MessageType type);

    /**
     * Kick a player from the server
     *
     * @param message The kick message
     */
    void kick(String message);

    /**
     * Is this player an OP
     *
     * @return true if this player is an OP
     */
    boolean isOp();

    /**
     * Set this player's OP status
     *
     * @param isOp the new OP status
     */
    void setOp(boolean isOp);
}
