package org.kilocraft.essentials.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.betterchairs.PlayerSitManager;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.io.IOException;
import java.util.*;

public class CommandSourceServerUser implements CommandSourceUser {
    private ServerCommandSource source;

    public CommandSourceServerUser(ServerCommandSource source) {
        this.source = source;
    }

    @Nullable
    @Override
    public UUID getUuid() {
        if (!CommandHelper.isConsole(this.source)) {
            try {
                return Objects.requireNonNull(this.getUser()).getUuid();
            } catch (CommandSyntaxException ignored) {
            }
        }

        return null;
    }

    @Override
    public String getUsername() {
        return this.source.getName();
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean hasNickname() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return source.getName();
    }

    @Override
    public String getFormattedDisplayName() {
        return getDisplayName();
    }

    @Override
    public Text getRankedDisplayName() {
        return new LiteralText(this.getDisplayName());
    }

    @Override
    public Text getRankedName() {
        return new LiteralText(this.getDisplayName());
    }

    @Override
    public String getNameTag() {
        return this.getDisplayName();
    }

    @Nullable
    @Override
    public List<String> getSubscriptionChannels() {
        return new ArrayList<String>(){{
            add(GlobalChat.getChannelId());
        }};
    }

    @Override
    public String getUpstreamChannelId() {
        return GlobalChat.getChannelId();
    }

    @Override
    public Optional<String> getNickname() {
        return Optional.empty();
    }

    @Override
    public Location getLocation() {
        return Vec3dLocation.of(this.source.getPosition());
    }

    @Override
    public @Nullable Location getLastSavedLocation() {
        return getLocation();
    }

    @Override
    public void saveLocation() {
    }

    @Override
    public void setNickname(String name) {
    }

    @Override
    public void clearNickname() {
    }

    @Override
    public void setLastLocation(Location loc) {
    }

    @Override
    public boolean canFly() {
        return false;
    }

    @Override
    public void setFlight(boolean set) {
    }

    @Override
    public boolean isSocialSpyOn() {
        return false;
    }

    @Override
    public void setSocialSpyOn(boolean on) {
    }

    @Override
    public boolean isCommandSpyOn() {
        return false;
    }

    @Override
    public void setCommandSpyOn(boolean on) {
    }

    @Override
    public boolean hasJoinedBefore() {
        return true;
    }

    @Override
    public @Nullable Date getFirstJoin() {
        return null;
    }

    @Override
    public void setUpstreamChannelId(String id) {
    }

    @Override
    public boolean isInvulnerable() {
        return false;
    }

    @Override
    public void setInvulnerable(boolean set) {
    }

    @Override
    public int getRTPsLeft() {
        return 0;
    }

    @Override
    public void setRTPsLeft(int amount) {
    }

    @Override
    public @Nullable UUID getLastPrivateMessageSender() {
        return null;
    }

    @Override
    public @Nullable String getLastPrivateMessage() {
        return null;
    }

    @Override
    public void setLastMessageSender(UUID uuid) {
    }

    @Override
    public void setLastPrivateMessage(String message) {

    }

    @Override
    public <F extends UserProvidedFeature> F feature(FeatureType<F> type) {
        return null;
    }

    @Override
    public UserHomeHandler getHomesHandler() {
        return null;
    }

    @Override
    public @Nullable String getLastSocketAddress() {
        return null;
    }

    @Override
    public GameMode getGameMode() {
        try {
            return CommandHelper.isConsole(this.source) ? GameMode.NOT_SET :
                    Objects.requireNonNull(this.getUser()).getGameMode();
        } catch (CommandSyntaxException ignored) {
        }

        return GameMode.NOT_SET;
    }

    @Override
    public void setGameMode(GameMode mode) {
        if (!CommandHelper.isConsole(this.source)) {
            try {
                Objects.requireNonNull(this.getUser()).setGameMode(mode);
            } catch (CommandSyntaxException ignore) {
            }
        }
    }

    @Override
    public boolean canSit() {
        return false;
    }

    @Override
    public void setCanSit(boolean set) {
    }

    @Override
    public int getMinutedPlayed() {
        return 0;
    }

    @Override
    public void setMinutesPlayed(int minutes) {
    }

    @Override
    public int getDisplayParticleId() {
        return 0;
    }

    @Override
    public void setDisplayParticleId(int i) {
    }

    @Override
    public void saveData() throws IOException {
    }

    @Nullable
    @Override
    public ServerPlayerEntity getPlayer() {
        try {
            return this.source.getPlayer();
        } catch (CommandSyntaxException ignored) {
            return null;
        }
    }

    @Override
    public ServerCommandSource getCommandSource() {
        return this.source;
    }

    @Override
    public void teleport(Location loc, boolean sendTicket) {
    }

    @Override
    public void sendMessage(String message) {
        KiloServer.getServer().sendMessage(message);
    }

    @Override
    public int sendError(String message) {
        this.source.sendError(new ChatMessage("&c" + message, true).toText());
        return -1;
    }

    @Override
    public int sendError(ExceptionMessageNode node, Object... objects) {
        String message = ModConstants.getMessageUtil().fromExceptionNode(node);
        KiloChat.sendMessageTo(this.source, new ChatMessage(
                (objects != null) ? String.format(message, objects) : message, true)
                .toText().formatted(Formatting.RED));
        return -1;
    }

    @Override
    public void sendMessage(Text text) {
        KiloChat.sendMessageToSource(this.source, text);
    }

    @Override
    public void sendMessage(ChatMessage chatMessage) {
        KiloChat.sendMessageToSource(this.source, chatMessage);
    }

    @Override
    public void sendLangMessage(String key, Object... objects) {
        KiloChat.sendLangMessageTo(this.source, key, objects);
    }

    @Override
    public void sendConfigMessage(String key, Object... objects) {
        String string = KiloConfig.getMessage(key, objects);
        KiloChat.sendMessageToSource(this.source, new ChatMessage(string, true));
    }

    @Nullable
    @Override
    public ClientConnection getConnection() {
        return null;
    }

    @Override
    public Vec3dLocation getLocationAsVector() {
        return null;
    }

    @Override
    public Vec3d getEyeLocation() {
        return new Vec3d(0, 0, 0);
    }

    @Override
    public void setSittingType(PlayerSitManager.SummonType type) {
    }

    @Override
    public @Nullable PlayerSitManager.SummonType getSittingType() {
        return null;
    }

    @Override
    public boolean isConsole() {
        return CommandHelper.isConsole(this.source);
    }

    @Override
    public @Nullable OnlineUser getUser() throws CommandSyntaxException {
        return KiloServer.getServer().getOnlineUser(source.getPlayer());
    }
}
