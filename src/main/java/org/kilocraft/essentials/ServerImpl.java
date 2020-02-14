package org.kilocraft.essentials;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.chat.ChatManager;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.EventRegistry;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.UserManager;
import org.kilocraft.essentials.events.server.ServerReloadEventImpl;
import org.kilocraft.essentials.mixin.accessor.MinecraftServerAccessor;
import org.kilocraft.essentials.servermeta.ServerMetaManager;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.TextFormatAnsiHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ServerImpl implements Server {
    private final MinecraftServer server;
    private final EventRegistry eventRegistry;
    private final String serverBrand;
    private String serverDisplayBrand;
    private String serverName = "Minecraft Server";
    private UserManager userManager;
    private ChatManager chatManager;
    private ServerMetaManager metaManager;
    private TextFormatAnsiHelper ansiHelper;

    public ServerImpl(MinecraftServer minecraftServer, EventRegistry eventManager, ServerUserManager serverUserManager, String serverBrand) {
        this.server = minecraftServer;
        this.serverBrand = serverBrand;
        this.userManager = serverUserManager;
        this.serverDisplayBrand = serverBrand;
        this.eventRegistry = eventManager;
        this.chatManager = new ChatManager();
        this.metaManager = new ServerMetaManager(server.getServerMetadata());
        this.ansiHelper = new TextFormatAnsiHelper();
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
    public void reload() {
        this.server.reload();
        KiloServer.getServer().triggerEvent(new ServerReloadEventImpl());
    }

    @Override
    public UserManager getUserManager() {
        return this.userManager;
    }

    @Override
    public OnlineUser getOnlineUser(String name) {
        return this.userManager.getOnline(name);
    }

    @Override
    public OnlineUser getOnlineUser(ServerPlayerEntity player) {
        return this.userManager.getOnline(player);
    }

    @Override
    public OnlineUser getOnlineUser(UUID uuid) {
        return this.userManager.getOnline(uuid);
    }

    @Override
    public CommandSourceUser getCommandSourceUser(ServerCommandSource source) {
        return new CommandSourceServerUser(source);
    }

    @Override
    public ChatManager getChatManager() {
        return this.chatManager;
    }

    @Override
    public Entity getEntity(UUID uuid) {
        for (ServerWorld world : this.getWorlds()) {
            if (world.getEntity(uuid) != null)
                return world.getEntity(uuid);
        }

        return null;
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
    public ServerWorld getWorld(DimensionType type) {
        return this.server.getWorld(type);
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
    public int execute(String command) {
        return server.getCommandManager().execute(server.getCommandSource(), command);
    }

    @Override
    public int execute(ServerCommandSource source, String command) {
        return KiloEssentials.getInstance().getCommandHandler().execute(source, command);
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
    public void sendGlobalPacket(Packet<?> packet) {
        for (ServerPlayerEntity playerEntity : this.server.getPlayerManager().getPlayerList()) {
            playerEntity.networkHandler.sendPacket(packet);
        }
    }

    @Override
    public void shutdown() {
        this.server.stop(false);
    }

    @Override
    public void restart() {
        shutdown();
        File marker = new File(FabricLoader.getInstance().getGameDirectory(), "RESTARTME");

        try {
            marker.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void restart(String reason) {
        kickAll(reason);
        restart();
    }

    @Override
    public void restart(Text reason) {
        kickAll(reason);
        restart();
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
            String str = TextFormat.removeAlternateColorCodes('&', line);
            getLogger().info(TextFormat.removeAlternateColorCodes(TextFormat.COLOR_CHAR, str));
        }

    }

    @Override
    public OperatorList getOperatorList() {
        return server.getPlayerManager().getOpList();
    }

    @Override
    public ServerMetaManager getMetaManager() {
        return this.metaManager;
    }

    @SuppressWarnings("untested")
    @Override
    public boolean supportsANSICodes() {
        //return System.console() != null && System.getenv().get("TERM") != null;
        return false;
    }

    public String getBrandName() {
        return serverBrand;
    }

    public TextFormatAnsiHelper getAnsiHelper() {
        return this.ansiHelper;
    }

}
