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
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.inventory.UserInventory;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.api.user.settting.UserSettings;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.io.IOException;
import java.util.*;

public  class CommandSourceServerUser implements CommandSourceUser {
    private ServerCommandSource source;

    public CommandSourceServerUser(ServerCommandSource source) {
        this.source = source;
    }

    @Nullable
    @Override
    public UUID getUuid() {
        if (!CommandUtils.isConsole(this.source)) {
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
    public UserSettings getSettings() {
        return null;
    }

    @Override
    public <T> T getSetting(Setting<T> setting) {
        return null;
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
    public boolean hasJoinedBefore() {
        return false;
    }

    @Override
    public @Nullable Date getFirstJoin() {
        return null;
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
    public int getTicksPlayed() {
        return 0;
    }

    @Override
    public void setTicksPlayed(int ticks) {

    }

    @Override
    public void saveData() throws IOException {

    }

    @Override
    public void trySave() throws CommandSyntaxException {

    }

    @Override
    public boolean equals(User anotherUser) {
        return this.source.getName().equals(anotherUser.getUsername());
    }

    @Override
    public @Nullable UserInventory getInventory() {
        return null;
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
        KiloChat.sendMessageTo(this.source, new LiteralText(TextFormat.translate(message)));
    }

    @Override
    public int sendError(String message) {
        this.source.sendError(new TextMessage("&c" + message, true).toText());
        return -1;
    }

    @Override
    public int sendLangError(String key, Object... objects) {
        this.sendMessage(LangText.getFormatter(true, key, objects));
        return -1;
    }

    @Override
    public int sendError(ExceptionMessageNode node, Object... objects) {
        String message = ModConstants.getMessageUtil().fromExceptionNode(node);
        KiloChat.sendMessageTo(this.source, new TextMessage(
                (objects != null) ? String.format(message, objects) : message, true)
                .toText().formatted(Formatting.RED));
        return -1;
    }

    @Override
    public void sendMessage(Text text) {
        this.source.sendFeedback(text, false);
    }

    @Override
    public void sendMessage(TextMessage textMessage) {
        KiloChat.sendMessageToSource(this.source, textMessage);
    }

    @Override
    public void sendLangMessage(String key, Object... objects) {
        KiloChat.sendLangMessageTo(this.source, key, objects);
    }

    @Override
    public void sendConfigMessage(String key, Object... objects) {
        String string = KiloConfig.getMessage(key, objects);
        KiloChat.sendMessageToSource(this.source, new TextMessage(string, true));
    }

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
        return null;
    }

    @Override
    public boolean hasPermission(CommandPermission perm) {
        if (this.isOnline() && !this.isConsole()) {
            try {
                return Objects.requireNonNull(this.getUser()).hasPermission(perm);
            } catch (CommandSyntaxException e) {
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(EssentialPermission perm) {
        if (this.isOnline() && !this.isConsole()) {
            try {
                return Objects.requireNonNull(this.getUser()).hasPermission(perm);
            } catch (CommandSyntaxException e) {
                return false;
            }
        }

        return false;
    }

    @Override
    public void setFlight(boolean set) {

    }

    @Override
    public void setGameMode(GameMode mode) {

    }

    @Override
    public boolean isConsole() {
        return CommandUtils.isConsole(this.source);
    }

    @Override
    public @Nullable OnlineUser getUser() throws CommandSyntaxException {
        return KiloServer.getServer().getOnlineUser(source.getPlayer());
    }
}
