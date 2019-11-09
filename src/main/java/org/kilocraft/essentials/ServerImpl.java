package org.kilocraft.essentials;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.EventRegistry;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.mixin.accessor.MinecraftServerAccessor;
import org.kilocraft.essentials.user.UserManager;

import java.util.*;

public class ServerImpl implements Server {
    private final MinecraftServer server;
    private final EventRegistry eventRegistry;
    private final UserManager userManager;
    private final String serverBrand;
    private String serverDisplayBrand;
    private String serverName = "Minecraft server";

    public ServerImpl(MinecraftServer minecraftServer, EventRegistry eventManager, UserManager userManager, String serverBrand) {
        this.server = minecraftServer;
        this.serverBrand = serverBrand;
        this.userManager = userManager;
        this.serverDisplayBrand = serverBrand;
        this.eventRegistry = eventManager;
    }

    public void savePlayers() {
    }

    @Override
    public MinecraftServer getVanillaServer() {
        return this.server;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return this.server.getPlayerManager();
    }

    @Override
    public UserManager getUserManager() {
        return this.userManager;
    }


    @Override
    public ServerPlayerEntity getPlayer(String name) {
        return getPlayerManager().getPlayer(name);
    }

    @Override
    public ServerPlayerEntity getPlayer(UUID uuid) {
        return getPlayerManager().getPlayer(uuid);
    }

    @Override
    public String getName() {
        return this.serverName;
    }

    @Override
    public void setName(String name) {
        this.serverName = name;
    }

    @Override
    public String getVersion() {
        return ModConstants.getVersion();
    }

    @Override
    public Logger getLogger() {
        return ((MinecraftServerAccessor) server).getLogger();
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
    public Iterable<ServerWorld> getWorlds() {
        return server.getWorlds();
    }

    @Override
    public boolean isMainThread() {
        MinecraftServerAccessor accessor = (MinecraftServerAccessor) this.server;
        return Thread.currentThread().equals(accessor.getServerThread());
    }

    @Override
    public void registerEvent(EventHandler e) {
        eventRegistry.register(e);
    }

    @Override
    public EventRegistry getEventRegistry() {
        return eventRegistry;
    }

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
    public void execute(String command) {
        server.getCommandManager().execute(server.getCommandSource(), command);
    }

    @Override
    public void execute(ServerCommandSource source, String command) {
        server.getCommandManager().execute(source, command);
    }

    @Override
    public void setDisplayBrandName(String brand) {
        this.serverDisplayBrand = brand;
    }

    @Override
    public String getDisplayBrandName() {
        return this.serverDisplayBrand;
    }

    @Override
    public void shutdown() {
        savePlayers();

        this.server.stop(false);
    }

    @Override
    public void shutdown(String reason) {
        kickAll(reason);
        shutdown();
    }

    @Override
    public void shutdown(Text reason) {
        kickAll(reason);
        shutdown();
    }

    @Override
    public void kickAll(String reason) {
        kickAll(new LiteralText(reason));
    }

    @Override
    public void kickAll(Text reason) {
        for(ServerPlayerEntity player : getPlayerManager().getPlayerList()) {
            player.networkHandler.disconnect(reason);
        }

    }

    @Override
    public void sendMessage(String message) {
        String[] lines = message.split("\n");

        for (String line : lines) {
            getLogger().trace(TextFormat.removeAlternateColorCodes('&', line));
        }

    }

    @Override
    public OperatorList getOperatorList() {
        return server.getPlayerManager().getOpList();
    }

    public String getBrandName() {
        return serverBrand;
    }
}
