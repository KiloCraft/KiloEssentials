package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.playtimecommands.PlaytimeCommands;
import org.kilocraft.essentials.user.setting.Settings;
import org.kilocraft.essentials.util.GlobalUtils;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;
import org.kilocraft.essentials.util.player.UserUtils;

import java.net.SocketAddress;
import java.util.Date;
import java.util.UUID;

public class OnlineServerUser extends ServerUser implements OnlineUser {

    @Override
    public ServerPlayerEntity asPlayer() {
        return KiloServer.getServer().getPlayer(this.uuid);
    }

    @Override
    public ServerCommandSource getCommandSource() {
        return this.asPlayer().getCommandSource();
    }

    @Override
    public void teleport(@NotNull final Location loc, final boolean sendTicket) {
        if (sendTicket) {
            loc.getWorld().getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, loc.toChunkPos(), 1, this.asPlayer().getEntityId());
        }

        this.asPlayer().teleport(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getRotation().getYaw(), loc.getRotation().getPitch());
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
        KiloChat.sendMessageTo(this.asPlayer(), new TextMessage(message, true));
    }

    @Override
    public int sendError(final String message) {
        KiloChat.sendMessageTo(this.asPlayer(), ((MutableText)new TextMessage("&c" + message, true).toText()).formatted(Formatting.RED));
        return 0;
    }

    @Override
    public void sendError(TextMessage message) {

    }

    @Override
    public void sendError(Text text) {
        KiloChat.sendMessageTo(this.asPlayer(), ((MutableText)text).formatted(Formatting.RED));
    }

    @Override
    public void sendLangError(String key, Object... objects) {
        this.sendError(ModConstants.translation(key, objects));
    }

    @Override
    public int sendError(final ExceptionMessageNode node, final Object... objects) {
        final String message = ModConstants.getMessageUtil().fromExceptionNode(node);
        KiloChat.sendMessageTo(this.asPlayer(), ((MutableText)new TextMessage(
                objects != null ? String.format(message, objects) : message, true)
                .toText()).formatted(Formatting.RED));
        return -1;
    }

    @Override
    public void sendMessage(final Text text) {
        KiloChat.sendMessageTo(this.asPlayer(), text);
    }

    @Override
    public void sendMessage(final TextMessage textMessage) {
        KiloChat.sendMessageTo(this.asPlayer(), textMessage);
    }

    @Override
    public void sendLangMessage(final String key, final Object... objects) {
        KiloChat.sendLangMessageTo(this.asPlayer(), key, objects);
    }

    @Override
    public void sendConfigMessage(final String key, final Object... objects) {
        final String message = KiloConfig.getMessage(key, objects);
        this.sendMessage(new TextMessage(message, true));
    }

    @Override
    public ClientConnection getConnection() {
        return this.asPlayer().networkHandler.connection;
    }

    @Override
    public Vec3dLocation getLocationAsVector() {
        return Vec3dLocation.of(this);
    }

    @Override
    public Location getLocation() {
        return Vec3dLocation.of(this.asPlayer());
    }

    @Override
    public Vec3d getEyeLocation() {
        Vec3d vec = this.asPlayer().getPos();
        return new Vec3d(vec.getX(), this.asPlayer().getEyeY(), this.asPlayer().getPos().getZ());
    }

    public static OnlineServerUser of(final UUID uuid) {
        return (OnlineServerUser) ServerUser.manager.getOnline(uuid);
    }

    public static OnlineServerUser of(final String name) {
        return (OnlineServerUser) ServerUser.manager.getOnline(name);
    }

    public static OnlineServerUser of(final GameProfile profile) {
        return OnlineServerUser.of(profile.getId());
    }

    public static OnlineServerUser of(final ServerPlayerEntity player) {
        return OnlineServerUser.of(player.getUuid());
    }

    public OnlineServerUser(final ServerPlayerEntity player) {
        super(player.getUuid());
        super.name = player.getEntityName();
    }

    @Override
    public void fromTag(@NotNull final CompoundTag tag) {
        // All the other serialization logic is handled.
        super.fromTag(tag);
    }

    @Override
    public String getNameTag() {
        return super.getNameTag();
    }

    @Override
    public void setFlight(final boolean set) {
        super.getSettings().set(Settings.CAN_FLY, true);
        this.asPlayer().abilities.allowFlying = set;
        this.asPlayer().abilities.flying = set;
        this.asPlayer().sendAbilitiesUpdate();
    }

    @Override
    public void setGameMode(GameMode mode) {
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
            super.lastSocketAddress = this.getConnection().getAddress().toString().replaceFirst("/", "");
        }

        return super.lastSocketAddress;
    }

    @Deprecated
    @Override
    public void saveData() {
    }

    public void onJoined() {
        this.setFlight(super.getSetting(Settings.CAN_FLY));

        SocketAddress socketAddress = GlobalUtils.getSocketAddress(super.uuid);
        if (socketAddress != null) {
            lastSocketAddress = socketAddress.toString().replaceFirst("/", "");
        }

        messageCooldown = 0;

        GameMode gameMode = super.getSetting(Settings.GAME_MODE);
        if (gameMode == GameMode.NOT_SET) {
            gameMode = this.asPlayer().interactionManager.getGameMode();
        }

        this.setGameMode(gameMode);
        super.getSettings().set(Settings.GAME_MODE, gameMode);

        if (ticksPlayed <= 0) {
            ticksPlayed = this.asPlayer().getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_ONE_MINUTE));
        } else {
            this.asPlayer().getStatHandler().setStat(this.asPlayer(), Stats.CUSTOM.getOrCreateStat(Stats.PLAY_ONE_MINUTE), ticksPlayed);
        }

        if (KiloEssentials.hasPermissionNode(this.getCommandSource(), EssentialPermission.STAFF)) {
            isStaff = true;
        }

    }

    public void onLeave() {
        super.lastOnline = new Date();
    }

    private static int tick = 0;
    public void onTick() {
        tick++;
        ticksPlayed++;

        if (messageCooldown > 0) {
            --messageCooldown;
        }

        if (tick >= 20) {
            tick = 0;
            //super.location = Vec3dLocation.of(this.asPlayer());

            if (PlaytimeCommands.isEnabled()) {
                PlaytimeCommands.getInstance().onUserPlaytimeUp(this, ticksPlayed);
            }
        }

    }

}
