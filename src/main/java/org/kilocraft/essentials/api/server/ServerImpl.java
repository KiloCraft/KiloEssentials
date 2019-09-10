package org.kilocraft.essentials.api.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.command.CommandRegistry;
import org.kilocraft.essentials.api.command.commandImpl.CommandRegistryImpl;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.EventRegistry;
import org.kilocraft.essentials.api.util.MinecraftServerLoggable;
import org.kilocraft.essentials.api.world.World;
import org.kilocraft.essentials.api.world.worldimpl.WorldImpl;

import java.util.*;

public class ServerImpl implements Server {

    private final MinecraftServer server;
    private final EventRegistry eventRegistry;
    private final String serverBrand;
    private String serverDisplayBrand;

    public ServerImpl(MinecraftServer server, EventRegistry eventManager, String serverBrand) {
        this.server = server;
        this.serverBrand = serverBrand;

        this.eventRegistry = eventManager;
    }

    @Override
    public MinecraftServer getVanillaServer() {
        return this.server;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getVersion() {
        return server.getVersion();
    }

    @Override
    public Logger getLogger() {
        return ((MinecraftServerLoggable) server).getLogger();
    }

    @Override
    public Collection<PlayerEntity> getPlayerList() {
        Set<PlayerEntity> players = new HashSet<>();

        server.getPlayerManager().getPlayerList().forEach(e ->
                players.add(e)
        );

        return players;
    }

    @Override
    public List<World> getWorlds() {
        List<World> worlds = new ArrayList<>();
        server.getWorlds().forEach(world -> worlds.add(new WorldImpl(world)));

        return worlds;
    }

    @Override
    public boolean isMainThread() {
        return Thread.currentThread().getName().equals("Server thread");
    }

    @Override
    public void registerEvent(EventHandler e) {
        eventRegistry.register(e);
    }

    @Override
    public EventRegistry getEventRegistry() {
        return eventRegistry;
    }

//    @Override
//    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
//
//    }

    @Override
    public <E extends Event> E triggerEvent(E e) {
        return eventRegistry.trigger(e);
    }

    @Override
    public Optional<PlayerEntity> getPlayerByName(String playerName) {
        ServerPlayerEntity e = server.getPlayerManager().getPlayer(playerName);
        if (e == null)
            return Optional.empty();

        return Optional.of(e);
    }

    @Override
    public void exec(String command) {
        server.getCommandManager().execute(server.getCommandSource(), command);
    }

    @Override
    public void setDisplayBrandName(String brand) {
        this.serverDisplayBrand = brand;
    }

    @Override
    public String getDisplayBrandName() {
        if (serverDisplayBrand.isEmpty()) return serverBrand;
        else return serverDisplayBrand;
    }

    public String getBrandName() {
        return serverBrand;
    }
}
