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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.betterchairs.PlayerSitManager;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.UUID;

public class OnlineServerUser extends ServerUser implements OnlineUser {
    private PlayerSitManager.SummonType sitState;

    public ServerPlayerEntity getPlayer() {
        return KiloServer.getServer().getPlayer(this.uuid);
    }

    public ServerCommandSource getCommandSource() {
        return this.getPlayer().getCommandSource();
    }

    @Override
    public void teleport(Location loc, boolean sendTicket) {
        if (sendTicket)
            loc.getWorld().getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, loc.toChunkPos(), 1, getPlayer().getEntityId());

        getPlayer().teleport(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getRotation().getYaw(), loc.getRotation().getPitch());
    }

    @Override
    public void sendMessage(String message) {
        KiloChat.sendMessageTo(this.getPlayer(), new ChatMessage(message, true));
    }

    @Override
    public int sendError(String message) {
        KiloChat.sendMessageTo(this.getPlayer(), new ChatMessage("&c" + message, true).toText().formatted(Formatting.RED));
        return -1;
    }

    @Override
    public int sendError(ExceptionMessageNode node, Object... objects) {
        String message = ModConstants.getMessageUtil().fromExceptionNode(node);
        KiloChat.sendMessageTo(this.getPlayer(), new ChatMessage(
                (objects != null) ? String.format(message, objects) : message, true)
                .toText().formatted(Formatting.RED));
        return -1;
    }

    @Override
    public void sendMessage(Text text) {
        KiloChat.sendMessageTo(this.getPlayer(), text);
    }

    @Override
    public void sendMessage(ChatMessage chatMessage) {
        KiloChat.sendMessageTo(this.getPlayer(), chatMessage);
    }

    @Override
    public void sendLangMessage(String key, Object... objects) {
        KiloChat.sendLangMessageTo(this.getPlayer(), key, objects);
    }

    @Override
    public void sendConfigMessage(String key, Object... objects) {
        String message = KiloConfig.getMessage(key, objects);
        this.sendMessage(new ChatMessage(message, true));
    }

    @Override
    public ClientConnection getConnection() {
        return this.getPlayer().networkHandler.client;
    }

    @Override
    public Vec3dLocation getLocationAsVector() {
        return Vec3dLocation.of(this);
    }

    @Override
    public Vec3d getEyeLocation() {
        Vec3d vec = getPlayer().getPos();
        return new Vec3d(vec.getX(), getPlayer().getEyeY(), getPlayer().getPos().getZ());
    }

    @Override
    public void setSittingType(PlayerSitManager.SummonType type) {
        this.sitState = type;
    }

    @Nullable
    @Override
    public PlayerSitManager.SummonType getSittingType() {
        return this.sitState;
    }

    public static OnlineServerUser of(UUID uuid) {
        return (OnlineServerUser) manager.getOnline(uuid);
    }

    public static OnlineServerUser of(String name) {
        return (OnlineServerUser) manager.getOnline(name);
    }

    public static OnlineServerUser of(GameProfile profile) {
        return of(profile.getId());
    }

    public static OnlineServerUser of(ServerPlayerEntity player) {
        return of(player.getUuid());
    }

    public OnlineServerUser(ServerPlayerEntity player) {
        super(player.getUuid());
        this.name = player.getEntityName();
    }

    @Override
    protected void deserialize(@NotNull CompoundTag tag) {
        // All the other serialization logic is handled.
        super.deserialize(tag);
    }

    @Override
    public String getNameTag() {
        return super.getNameTag();
    }

    @Override
    public void setFlight(boolean set) {
        super.setFlight(true);
        this.getPlayer().abilities.allowFlying = set;
        this.getPlayer().abilities.flying = set;
        this.getPlayer().sendAbilitiesUpdate();
    }

    @Deprecated
    @Override
    public void saveData() {
    }

    public void onJoined() {
        if (this.canFly() && KiloCommands.hasPermission(this.getCommandSource(), CommandPermission.FLY_SELF))
            this.setFlight(true);

        super.lastSocketAddress = this.getConnection().getAddress().toString();
        super.gameMode = this.getGameMode();
        if (super.ticksPlayed != -1)
            super.ticksPlayed = this.getPlayer().getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_ONE_MINUTE));
    }

    public void onTick() {
        super.ticksPlayed++;
        super.resetMessageCooldown();
        super.updateLocation();
    }

}
