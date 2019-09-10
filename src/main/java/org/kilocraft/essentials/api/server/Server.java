package org.kilocraft.essentials.api.server;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.EventRegistry;
import org.kilocraft.essentials.api.world.World;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Server {

    /**
     *  Gets the Minecraft Server
     *
     * @return instance of MinecraftServer
     */
    MinecraftServer getVanillaServer();

    /**
     * Gets the Server name
     *
     * @return the name of the Server
     */
    String getName();

    /**
     * Gets the version of the Server
     *
     * @return the version of the Server
     */
    String getVersion();

    /**
     * Gets the logger of the Server
     *
     * @return the logger of the Server
     */
    Logger getLogger();

    /**
     * Gets all the online players in the Server
     *
     * @return online players in the Server
     */
    Collection<PlayerEntity> getPlayerList();

    /**
     * Gets all worlds in this Server
     *
     * @return all worlds in this Server
     */
    List<World> getWorlds();

    /**
     * Checks if we are running inside the Server's main thread
     *
     * @return are we running inside the main thread
     */
    boolean isMainThread();

    /**
     * Registers an event
     *
     * @param e event to register
     */
    void registerEvent(EventHandler e);

    /**
     * Gets the EventRegistry
     *
     * @return eventRegistry
     */
    EventRegistry getEventRegistry();

    /**
     * Registers a command
     *
     * @param dispatcher command source dispatcher to register
     */
    //void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher);

    /**
     * Triggers an event
     *
     * @param <E> The event type to trigger
     * @param e   Event to trigger.
     * @return The event instance, with any modifications applied by event handlers
     */
    <E extends Event> E triggerEvent(E e);

    /**
     * Gets a player by name
     *
     * @param playerName Name of the player to get
     * @return the player instance
     */
    Optional<PlayerEntity> getPlayerByName(String playerName);

    /**
     * Execute a console command
     *
     * @param command Command to execute
     */
    void exec(String command);

    /**
     * Sets the brand name of the server
     *
     * @param brand brandName
     */

    void setDisplayBrandName(String brand);

    /**
     * Gets the brand name of the server
     *
     * @return ""
     */

    String getDisplayBrandName();

    String getBrandName();

}