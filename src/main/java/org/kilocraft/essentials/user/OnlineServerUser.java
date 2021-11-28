package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.StringUtils;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.extensions.playtimecommands.PlaytimeCommands;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.Format;
import org.kilocraft.essentials.util.commands.KiloCommands;

import java.net.SocketAddress;
import java.util.Date;
import java.util.UUID;

public class OnlineServerUser extends ServerUser implements OnlineUser {

    private final ServerPlayer serverPlayerEntity;

    @Override
    public ServerPlayer asPlayer() {
        final ServerPlayer player = KiloEssentials.getMinecraftServer().getPlayerList().getPlayer(this.uuid);
        return player != null ? player : this.serverPlayerEntity;
    }

    @Override
    public CommandSourceStack getCommandSource() {
        return this.asPlayer().createCommandSourceStack();
    }

    @Override
    public void sendSystemMessage(Object sysMessage) {
        super.systemMessageCoolDown += 20;
        if (super.systemMessageCoolDown > ServerUser.SYS_MESSAGE_COOL_DOWN) {
            if (sysMessage instanceof String) {
                this.sendMessage((String) sysMessage);
            } else if (sysMessage instanceof net.minecraft.network.chat.Component) {
                this.sendMessage((net.minecraft.network.chat.Component) sysMessage);
            } else {
                this.sendMessage(String.valueOf(sysMessage));
            }
        }
    }

    @Override
    public void teleport(@NotNull final Location loc, final boolean sendTicket) {
        if (sendTicket) {
            loc.getWorld().getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, loc.toChunkPos(), 0, this.asPlayer().getId());
        }

        this.asPlayer().teleportTo(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getRotation().getYaw(), loc.getRotation().getPitch());
    }

    @Override
    public void teleport(@NotNull OnlineUser target) {
        this.teleport(Vec3dLocation.of(target), true);
    }

    @Override
    public String getName() {
        return super.name;
    }

    @Override
    public UUID getId() {
        return super.uuid;
    }

    @Override
    public void sendMessage(final String message) {
        this.sendMessage(ComponentText.of(message));
    }

    @Override
    public int sendError(final String message) {
        this.sendMessage(ComponentText.of(message).color(NamedTextColor.RED));
        return 0;
    }

    @Override
    public void sendPermissionError(@NotNull String hover) {
        this.sendMessage(ComponentText.of(ModConstants.translation("command.exception.permission")).style(style -> style.hoverEvent(HoverEvent.showText(Component.text(hover)))));
    }

    @Override
    public void sendLangError(@NotNull String key, Object... objects) {
        this.sendError(ModConstants.translation(key, objects));
    }

    @Override
    public void sendMessage(final net.minecraft.network.chat.Component text) {
        this.asPlayer().displayClientMessage(text, false);
    }

    @Override
    public void sendMessage(@NotNull Component component) {
        this.sendMessage(ComponentText.toText(component));
    }

    @Override
    public void sendLangMessage(final @NotNull String key, final Object... objects) {
        this.sendMessage(ModConstants.translation(key, objects));
    }

    @Override
    public Connection getConnection() {
        return this.asPlayer().connection.connection;
    }

    @Override
    public Vec3dLocation getLocationAsVector() {
        return Vec3dLocation.of(this);
    }

    @Override
    public Location getLocation() {
        return Vec3dLocation.of(this.asPlayer());
    }

    public static OnlineServerUser of(final UUID uuid) {
        return (OnlineServerUser) ServerUser.MANAGER.getOnline(uuid);
    }

    public static OnlineServerUser of(final String name) {
        return (OnlineServerUser) ServerUser.MANAGER.getOnline(name);
    }

    public static OnlineServerUser of(final GameProfile profile) {
        return OnlineServerUser.of(profile.getId());
    }

    public static OnlineServerUser of(final ServerPlayer player) {
        return OnlineServerUser.of(player.getUUID());
    }

    public OnlineServerUser(final ServerPlayer player) {
        super(player.getUUID());
        super.name = player.getScoreboardName();
        this.serverPlayerEntity = player;
    }

    @Override
    public void fromTag(@NotNull final CompoundTag tag) {
        // All the other serialization logic is handled.
        super.fromTag(tag);
    }

    @Override
    public void setFlight(final boolean set) {
        this.asPlayer().getAbilities().mayfly = set;
        this.asPlayer().getAbilities().flying = set;
        this.asPlayer().onUpdateAbilities();
    }

    @Override
    public void setGameMode(GameType mode) {
        this.asPlayer().setGameMode(mode);
    }

    @Override
    public boolean hasPermission(final CommandPermission perm) {
        return KiloCommands.hasPermission(this.getCommandSource(), perm);
    }

    @Override
    public boolean hasPermission(final EssentialPermission perm) {
        return KiloEssentials.hasPermissionNode(this.getCommandSource(), perm);
    }

    @Override
    public String getLastSocketAddress() {
        if (this.getConnection() != null) {
            super.lastSocketAddress = this.getConnection().getRemoteAddress().toString().replaceFirst("/", "");
        }

        return super.lastSocketAddress;
    }

    @Nullable
    @Override
    public String getLastIp() {
        String last = this.getLastSocketAddress();
        if (last == null) {
            return null;
        }

        return StringUtils.socketAddressToIp(this.getLastSocketAddress());
    }

    @Deprecated
    @Override
    public void saveData() {
    }

    public void onJoined() {
        SocketAddress socketAddress = this.getConnection().getRemoteAddress();
        if (socketAddress != null) {
            this.lastSocketAddress = socketAddress.toString().replaceFirst("/", "");
        }

        super.messageCoolDown = 0;
        super.systemMessageCoolDown = 0;

        if (this.ticksPlayed <= 0) {
            this.ticksPlayed = this.asPlayer().getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
        } else {
            this.asPlayer().getStats().setValue(this.asPlayer(), Stats.CUSTOM.get(Stats.PLAY_TIME), this.ticksPlayed);
        }

        this.isStaff = KiloEssentials.hasPermissionNode(this.getCommandSource(), EssentialPermission.STAFF);

        if (KiloCommands.hasPermission(this.getCommandSource(), CommandPermission.NICKNAME_SELF) || KiloCommands.hasPermission(this.getCommandSource(), CommandPermission.NICKNAME_OTHERS)) {
            this.getPreference(Preferences.NICK).ifPresent(oldNickname -> this.setNickname(Format.validatePermission(this, oldNickname, CommandPermission.PERMISSION_PREFIX + "nickname.formatting")));
        } else {
            this.clearNickname();
        }
    }

    public void onLeave() {
        super.lastOnline = new Date();
    }

    private static int tick = 0;

    public void onTick() {
        tick++;
        this.ticksPlayed++;

        if (this.messageCoolDown > 0) {
            --this.messageCoolDown;
        }

        if (this.systemMessageCoolDown > 0) {
            --this.systemMessageCoolDown;
        }

        if (tick >= 20) {
            tick = 0;
            if (this.asPlayer() != null) {
                super.location = Vec3dLocation.of(this.asPlayer());
            }

            if (PlaytimeCommands.isEnabled()) {
                PlaytimeCommands.getInstance().onUserPlaytimeUp(this, this.ticksPlayed);
            }
        }

    }

}
