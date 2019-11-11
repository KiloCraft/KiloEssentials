package org.kilocraft.essentials.api.server;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.EventRegistry;
import org.kilocraft.essentials.api.user.UserManager;
import org.kilocraft.essentials.user.ServerUserManager_Old;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface Server {

    /**
     * Gets the Minecraft Server
     *
     * @return instance of MinecraftServer
     */
    MinecraftServer getVanillaServer();

    /**
     * Gets the PlayerManager of the VanillaServer
     *
     * @return instance of PlayerManager
     */

    PlayerManager getPlayerManager();

    UserManager getUserManager();

    /**
     * Gets the KiloServer's UserManager
     *
     * @return instance of UserManager
     */
    @Deprecated
    ServerUserManager_Old getUserManagerOLD();

    /**
     * Gets a player object by the given username.
     * <p>
     * This method may not return objects for offline players.
     *
     * @param name the name to look up
     * @return a player if one was found, null otherwise
     */
    ServerPlayerEntity getPlayer(String name);

    /**
     * Gets the player with the given UUID.
     *
     * @param uuid UUID of the player to retrieve
     * @return a player object if one was found, null otherwise
     */
    ServerPlayerEntity getPlayer(UUID uuid);

    /**
     * Gets the Server name
     *
     * @return the name of the Server
     */
    String getName();

    /**
     * Sets the Server name
     */
    void setName(String name);

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
    Iterable<ServerWorld> getWorlds();

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
    void execute(String command);

    /**
     * Execute a command
     * @param source source (usually player) to execute the command
     * @param command the string that contains the command to execute
     */
    void execute(ServerCommandSource source, String command);

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
    String getBrandName();

    /**
     * Gets the custom brand name of the server
     *
     * @return ""
     */
    String getDisplayBrandName();

    /**
     * Stops the server
     */
    void shutdown();

    /**
     * Stops the server
     * @param reason is used for kicking the players
     */
    void shutdown(String reason);

    void shutdown(Text reason);

    /**
     * Kicks all the players on the server
     * @param reason to kick the player
     */
    void kickAll(String reason);

    void kickAll(Text reason);

    /**
     * Sends a message to console
     *
     * @param message you want to send
     */
    void sendMessage(String message);

    /**
     * Gets the OperatorList
     * @return a instance of OperatorList
     */
    OperatorList getOperatorList();
}