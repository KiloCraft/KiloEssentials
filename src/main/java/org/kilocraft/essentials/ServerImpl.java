package org.kilocraft.essentials;

import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.SharedConstants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.EventRegistry;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.UserManager;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.events.server.ServerReloadEventImpl;
import org.kilocraft.essentials.mixin.accessor.MinecraftServerAccessor;
import org.kilocraft.essentials.servermeta.ServerMetaManager;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.Action;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class ServerImpl implements Server {
    private final MinecraftServer server;
    private final EventRegistry eventRegistry;
    private final UserManager userManager;
    private final ServerMetaManager metaManager;
    private final String serverBrand;
    private String serverDisplayBrand;
    private String serverName = "Minecraft Server";


    public ServerImpl(@NotNull final MinecraftServer nms,
                      @NotNull final EventRegistry eventManager,
                      @NotNull final ServerUserManager userManager,
                      @NotNull final String brand) {
        this.server = nms;
        this.serverBrand = brand;
        this.userManager = userManager;
        this.serverDisplayBrand = brand;
        this.eventRegistry = eventManager;
        this.metaManager = new ServerMetaManager(server.getServerMetadata());
    }

    @Override
    public @NotNull MinecraftServer getMinecraftServer() {
        return this.server;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return this.server.getPlayerManager();
    }

    @Override
    public void reload(Action<Throwable> fallback) {
        this.reloadKiloEssentials();
        this.reloadMinecraftServer(fallback);
    }

    @Override
    public void reloadKiloEssentials() {
        KiloServer.getServer().triggerEvent(new ServerReloadEventImpl(this.server));
    }

    @Override
    public void reloadMinecraftServer(Action<Throwable> fallback) {
        ResourcePackManager resourcePackManager = this.server.getDataPackManager();
        SaveProperties saveProperties = this.getMinecraftServer().getSaveProperties();
        Collection<String> collection = resourcePackManager.getEnabledNames();

        Collection<String> modifiedCollection = Lists.newArrayList(collection);
        resourcePackManager.scanPacks();
        for (String string : resourcePackManager.getNames()) {
            if (!saveProperties.getDataPackSettings().getDisabled().contains(string) && !modifiedCollection.contains(string)) {
                modifiedCollection.add(string);
            }
        }

        this.server.reloadResources(collection).exceptionally((throwable) -> {
            fallback.perform(throwable);
            return null;
        });
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
        return ((MinecraftServerAccessor) this.server).getLogger();
    }

    @Override
    public Collection<PlayerEntity> getPlayerList() {
        return new HashSet<>(server.getPlayerManager().getPlayerList());
    }

    @Override
    public Iterable<ServerWorld> getWorlds() {
        return server.getWorlds();
    }

    @Override
    public ServerWorld getWorld(RegistryKey<World> key) {
        return this.server.getWorld(key);
    }

    @Override
    public boolean isMainThread() {
        MinecraftServerAccessor accessor = (MinecraftServerAccessor) this.server;
        return Thread.currentThread().equals(accessor.getServerThread());
    }

    @Override
    public <E extends Event> void registerEvent(EventHandler<E> e) {
        if (SharedConstants.isDevelopment) KiloEssentials.getLogger().info("Registering event " + e.getClass().getSimpleName());
        eventRegistry.register(e);
    }

    @Override
    public EventRegistry getEventRegistry() {
        return eventRegistry;
    }

    @Override
    public <E extends Event> E triggerEvent(@NotNull E e) {
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
        for (ServerPlayerEntity player : getPlayerManager().getPlayerList()) {
            player.networkHandler.disconnect(reason);
        }

    }

    @Override
    public void sendMessage(String message) {
        for (String s : message.split("\n")) {
            getLogger().info(TextFormat.clearColorCodes(s));
        }
    }

    @Override
    public void sendMessage(Text text) {
        this.server.sendSystemMessage(text, Util.NIL_UUID);
    }

    @Override
    public void sendMessage(@NotNull Component component) {
        this.sendMessage(ComponentText.toText(component));
    }

    @Override
    public void sendLangMessage(@NotNull String key, @Nullable Object... objects) {
    }

    @Override
    public int sendError(String message) {
        this.sendMessage(message);
        return -1;
    }

    @Override
    public void sendPermissionError(@NotNull String hover) {
        this.sendMessage(Component.text(KiloChat.getFormattedLang("command.exception.permission")).style(style -> style.hoverEvent(HoverEvent.showText(Component.text(hover)))));
    }

    @Override
    public void sendLangError(@NotNull String key, @Nullable Object... objects) {
    }

    @Override
    public void sendWarning(String message) {
        for (String s : message.split("\n")) {
            getLogger().warn(TextFormat.clearColorCodes(s));
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

    public String getBrandName() {
        return serverBrand;
    }

}
