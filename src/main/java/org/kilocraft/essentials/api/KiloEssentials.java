package org.kilocraft.essentials.api;

import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.feature.ConfigurableFeatures;
import org.kilocraft.essentials.api.util.EntityCommands;
import org.kilocraft.essentials.api.util.TickManager;
import org.kilocraft.essentials.api.util.tablist.LuckpermsTabListData;
import org.kilocraft.essentials.api.util.tablist.TabListData;
import org.kilocraft.essentials.compability.DiscordFabModule;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.events.ServerEvents;
import org.kilocraft.essentials.extensions.betterchairs.SeatManager;
import org.kilocraft.essentials.extensions.customcommands.CustomCommands;
import org.kilocraft.essentials.extensions.magicalparticles.ParticleAnimationManager;
import org.kilocraft.essentials.extensions.playtimecommands.PlaytimeCommands;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.extensions.warps.serverwidewarps.ServerWarpManager;
import org.kilocraft.essentials.extensions.warps.serverwidewarps.WarpCommand;
import org.kilocraft.essentials.provided.BrandedServer;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.ExtraGameRules;
import org.kilocraft.essentials.util.NbtCommands;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class KiloEssentials {

    private static final Logger LOGGER = LogManager.getLogger("KiloEssentials");
    private static KiloEssentials INSTANCE;
    private final ServerUserManager userManager;
    private TabListData tabListData;
    private DedicatedServer dedicatedServer;

    public KiloEssentials() {
        INSTANCE = this;
        this.userManager = new ServerUserManager();
        ExtraGameRules.initialize();
        KiloConfig.load();
        this.registerEvents();
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static KiloEssentials getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        throw new RuntimeException("Its too early to get a static instance of KiloEssentials!");
    }

    public static boolean hasPermissionNode(CommandSourceStack src, EssentialPermission perm) {
        return hasPermissionNode(src, perm, 2);
    }

    public static boolean hasPermissionNode(CommandSourceStack src, CommandPermission perm) {
        return hasPermissionNode(src, perm.getNode(), 2);
    }

    public static boolean hasPermissionNode(CommandSourceStack src, EssentialPermission perm, int minOpLevel) {
        return hasPermissionNode(src, perm.getNode(), minOpLevel);
    }

    public static boolean hasPermissionNode(CommandSourceStack src, CommandPermission perm, int minOpLevel) {
        return hasPermissionNode(src, perm.getNode(), minOpLevel);
    }

    public static boolean hasPermissionNode(CommandSourceStack src, String perm) {
        return hasPermissionNode(src, perm, 2);
    }

    public static boolean hasPermissionNode(CommandSourceStack src, String perm, int minOpLevel) {
        try {
            return Permissions.check(src, perm, minOpLevel);
        } catch (Throwable e) {
            return src.hasPermission(minOpLevel);
        }
    }

    public static DedicatedServer getMinecraftServer() {
        return getInstance().dedicatedServer;
    }

    public static ServerUserManager getUserManager() {
        return getInstance().userManager;
    }

    public static Path getDataDirPath() {
        return getEssentialsPath().resolve("data");
    }

    public static Path getEssentialsPath() {
        return new File(getWorkingDirectory()).toPath().resolve("essentials");
    }

    public static Path getLangDirPath() {
        return getEssentialsPath().resolve("lang");
    }

    public static String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    public static void registerFeatures() {
        ConfigurableFeatures.register(new UserHomeHandler(), "playerHomes");
        ConfigurableFeatures.register(new ServerWarpManager(), "serverWideWarps");
        ConfigurableFeatures.register(new PlayerWarpsManager(), "playerWarps");
        ConfigurableFeatures.register(new SeatManager(), "betterChairs");
        ConfigurableFeatures.register(new CustomCommands(), "customCommands");
        ConfigurableFeatures.register(new ParticleAnimationManager(), "magicalParticles");
        ConfigurableFeatures.register(new PlaytimeCommands(), "playtimeCommands");
    }

    public void sendGlobalPacket(Packet<?> packet) {
        for (ServerPlayer playerEntity : this.dedicatedServer.getPlayerList().getPlayers()) {
            playerEntity.connection.send(packet);
        }
    }

    private void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.dedicatedServer = (DedicatedServer) server);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> this.onReady((DedicatedServer) server));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((ignored, ignored2, ignored3) -> this.reload());
        ServerLifecycleEvents.SERVER_STOPPING.register(ignored -> this.onStop());
        ServerTickEvents.START_SERVER_TICK.register(ignored -> this.onTick());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> this.onJoin(handler.getPlayer()));
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> this.onLeave(handler.getPlayer()));
        ServerEvents.SAVE.register(s -> this.onSave());
        NbtCommands.registerEvents();
        EntityCommands.registerEvents();
        DiscordFabModule.registerEvents();
    }

    public boolean hasLuckPerms() {
        try {
            LuckPermsProvider.get();
            return true;
        } catch (NoClassDefFoundError | IllegalStateException exception) {
            return false;
        }
    }

    private void onJoin(ServerPlayer player) {
        this.userManager.onJoin(player);
        BrandedServer.provide(player);
        this.tabListData.onJoin(player);
    }

    private void onLeave(ServerPlayer player) {
        this.tabListData.onLeave(player);
    }

    private void onReady(ServerPlayer player) {
        this.userManager.onReady(player);
    }

    private void load() {
        new ServerSettings();
        ServerSettings.registerSettings();
        NBTStorageUtil.onLoad();
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        ModConstants.loadConstants();
        KiloCommands.registerCommands(dispatcher);
        registerFeatures();
    }

    public void reload() {
        KiloConfig.load();
        BrandedServer.update();
        ModConstants.loadFiles();
        ConfigurableFeatures.loadAll(true);
        NBTStorageUtil.onSave();
    }

    private void onReady(DedicatedServer server) {
        this.load();
        this.tabListData = this.hasLuckPerms() ? new LuckpermsTabListData(this) : new TabListData(this);
        this.userManager.onServerReady();
        WarpCommand.registerAliases();
        ConfigurableFeatures.loadAll(false);
        try {
            this.userManager.getMutedPlayerList().load();
        } catch (IOException e) {
            KiloEssentials.getLogger().error("An unexpected error occurred while loading the Muted Player List", e);
        }
    }

    private void onTick() {
        TickManager.onTick();
        int ticks = getMinecraftServer().getTickCount();
        if (ticks % KiloConfig.main().playerList().updateRate == 0) {
            this.tabListData.onUpdate();
        }
        this.userManager.onTick();
        ConfigurableFeatures.onTick();

    }

    private void onStop() {
        if (SeatManager.isEnabled()) {
            SeatManager.getInstance().killAll();
        }
    }

    private void onSave() {
        NBTStorageUtil.onSave();
        this.userManager.saveAllUsers();
    }
}
