package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.stat.Stats;
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
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.setting.Settings;
import org.kilocraft.essentials.util.GlobalUtils;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.net.SocketAddress;
import java.util.UUID;

public class OnlineServerUser extends ServerUser implements OnlineUser {

    @Override
    public ServerPlayerEntity getPlayer() {
        return KiloServer.getServer().getPlayer(this.uuid);
    }

    @Override
    public ServerCommandSource getCommandSource() {
        return this.getPlayer().getCommandSource();
    }

    @Override
    public void teleport(final Location loc, final boolean sendTicket) {
        if (sendTicket)
            loc.getWorld().getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, loc.toChunkPos(), 1, this.getPlayer().getEntityId());

        this.getPlayer().teleport(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getRotation().getYaw(), loc.getRotation().getPitch());
    }

    @Override
    public void sendMessage(final String message) {
        KiloChat.sendMessageTo(this.getPlayer(), new ChatMessage(message, true));
    }

    @Override
    public int sendError(final String message) {
        KiloChat.sendMessageTo(this.getPlayer(), new ChatMessage("&c" + message, true).toText().formatted(Formatting.RED));
        return -1;
    }

    @Override
    public int sendError(final ExceptionMessageNode node, final Object... objects) {
        final String message = ModConstants.getMessageUtil().fromExceptionNode(node);
        KiloChat.sendMessageTo(this.getPlayer(), new ChatMessage(
                objects != null ? String.format(message, objects) : message, true)
                .toText().formatted(Formatting.RED));
        return -1;
    }

    @Override
    public void sendMessage(final Text text) {
        KiloChat.sendMessageTo(this.getPlayer(), text);
    }

    @Override
    public void sendMessage(final ChatMessage chatMessage) {
        KiloChat.sendMessageTo(this.getPlayer(), chatMessage);
    }

    @Override
    public void sendLangMessage(final String key, final Object... objects) {
        KiloChat.sendLangMessageTo(this.getPlayer(), key, objects);
    }

    @Override
    public void sendConfigMessage(final String key, final Object... objects) {
        final String message = KiloConfig.getMessage(key, objects);
        this.sendMessage(new ChatMessage(message, true));
    }

    @Override
    public ClientConnection getConnection() {
        return this.getPlayer().networkHandler.connection;
    }

    @Override
    public Vec3dLocation getLocationAsVector() {
        return Vec3dLocation.of(this);
    }

    @Override
    public Vec3d getEyeLocation() {
        final Vec3d vec = this.getPlayer().getPos();
        return new Vec3d(vec.getX(), this.getPlayer().getEyeY(), this.getPlayer().getPos().getZ());
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
        super(player.getUuid(), player);
        super.name = player.getEntityName();
    }

    @Override
    protected void deserialize(@NotNull final CompoundTag tag) {
        // All the other serialization logic is handled.
        super.deserialize(tag);
    }

    @Override
    public String getNameTag() {
        return super.getNameTag();
    }

    @Override
    public void setFlight(final boolean set) {
        super.getSettings().set(Settings.CAN_FLY, true);
        this.getPlayer().abilities.allowFlying = set;
        this.getPlayer().abilities.flying = set;
        this.getPlayer().sendAbilitiesUpdate();
    }

    @Override
    public void setGameMode(GameMode mode) {
        this.getPlayer().setGameMode(mode);
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
            gameMode = this.getPlayer().interactionManager.getGameMode();
        }

        this.setGameMode(gameMode);
        super.getSettings().set(Settings.GAME_MODE, gameMode);

        if (ticksPlayed <= 0) {
            ticksPlayed = this.getPlayer().getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_ONE_MINUTE));
        } else {
            this.getPlayer().getStatHandler().setStat(this.getPlayer(), Stats.CUSTOM.getOrCreateStat(Stats.PLAY_ONE_MINUTE), ticksPlayed);
        }

        if (KiloEssentials.hasPermissionNode(this.getCommandSource(), EssentialPermission.STAFF)) {
            isStaff = true;
        }

    }

    public void onTick() {
        ticksPlayed++;
        updateLocation();
        messageCooldown++;

        if (messageCooldown > 0) {
            --messageCooldown;
        }
    }

}
