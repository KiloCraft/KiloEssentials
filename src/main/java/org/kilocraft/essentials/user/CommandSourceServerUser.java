package org.kilocraft.essentials.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CommandSourceServerUser implements CommandSourceUser {
    private ServerCommandSource source;

    public CommandSourceServerUser(ServerCommandSource source) {
        this.source = source;
    }

    @Override
    public UUID getUuid() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public boolean hasNickname() {
        return false;
    }

    @Override
    public String getDisplayname() {
        return source.getName();
    }

    @Override
    public String getFormattedDisplayname() {
        return getDisplayname();
    }

    @Override
    public Text getRankedDisplayname() {
        return null;
    }

    @Override
    public List<String> getSubscriptionChannels() {
        return null;
    }

    @Override
    public String getUpstreamChannelId() {
        return null;
    }

    @Override
    public Optional<String> getNickname() {
        return Optional.empty();
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public @Nullable Location getLastSavedLocation() {
        return null;
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
        return false;
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
    public boolean canSit() {
        return false;
    }

    @Override
    public void setCanSit(boolean set) {
    }

    @Override
    public int getDisplayParticleId() {
        return 0;
    }

    @Override
    public void setDisplayParticleId(int i) {
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
        String string = KiloConfig.getProvider().getMessages().getMessage(key, objects);
        KiloChat.sendMessageToSource(this.source, new ChatMessage(string, true));
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
