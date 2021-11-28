package org.kilocraft.essentials.user;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.preference.Preference;
import org.kilocraft.essentials.api.user.preference.UserPreferences;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CommandSourceServerUser implements CommandSourceUser {
    private final CommandSourceStack source;

    private CommandSourceServerUser(CommandSourceStack source) {
        this.source = source;
    }

    public static CommandSourceServerUser of(CommandSourceStack source) {
        return new CommandSourceServerUser(source);
    }

    public static CommandSourceServerUser of(CommandContext<CommandSourceStack> context) {
        return new CommandSourceServerUser(context.getSource());
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
        return this.source.getTextName();
    }

    @Override
    public UserPreferences getPreferences() {
        return null;
    }

    @Override
    public <T> T getPreference(Preference<T> preference) {
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
        return this.source.getTextName();
    }

    @Override
    public String getFormattedDisplayName() {
        return this.getDisplayName();
    }

    @Override
    public net.minecraft.network.chat.Component getRankedDisplayName() {
        return new TextComponent(this.getDisplayName());
    }

    @Override
    public String getRankedDisplayNameAsString() {
        return this.getDisplayName();
    }

    @Override
    public net.minecraft.network.chat.Component getRankedName() {
        return new TextComponent(this.getDisplayName());
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
    public void setNickname(String name) {

    }

    @Override
    public Location getLocation() {
        return Vec3dLocation.of(this.source.getPosition());
    }

    @Override
    public @Nullable Location getLastSavedLocation() {
        return this.getLocation();
    }

    @Override
    public void saveLocation() {

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
    public @Nullable Date getLastOnline() {
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
    public @Nullable String getLastIp() {
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
    public void saveData() {

    }

    @Override
    public void trySave() {

    }

    @Override
    public boolean equals(User anotherUser) {
        return this.source.getTextName().equals(anotherUser.getUsername());
    }

    @Override
    public EntityIdentifiable getLastMessageReceptionist() {
        if (this.isOnline()) {
            try {
                assert this.getUser() != null;
                return this.getUser().getLastMessageReceptionist();
            } catch (CommandSyntaxException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setLastMessageReceptionist(EntityIdentifiable entity) {
        if (this.isOnline()) {
            try {
                assert this.getUser() != null;
                this.getUser().setLastMessageReceptionist(entity);
            } catch (CommandSyntaxException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public ServerPlayer asPlayer() {
        try {
            return this.source.getPlayerOrException();
        } catch (CommandSyntaxException ignored) {
            return null;
        }
    }

    @Override
    public CommandSourceStack getCommandSource() {
        return this.source;
    }

    @Override
    public void sendSystemMessage(Object sysMessage) {
        try {
            Objects.requireNonNull(this.getUser()).sendSystemMessage(sysMessage);
        } catch (CommandSyntaxException ignored) {
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
    public void teleport(@NotNull Location loc, boolean sendTicket) {
    }

    @Override
    public void teleport(@NotNull OnlineUser user) {
    }

    @Override
    public void sendMessage(String message) {
        MutableComponent text = new TextComponent(CommandUtils.isConsole(this.source) ? "\n" : "");
        this.sendMessage(text.append(ComponentText.toText(message).withStyle(style -> {
            if (SharedConstants.IS_RUNNING_IN_IDE && this.hasPermission(EssentialPermission.DEBUG)) {
                return style.withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, new TextComponent(message)));
            }
            return style;
        })));
    }

    @Override
    public int sendError(String message) {
        this.sendMessage(ComponentText.of(message).color(NamedTextColor.RED));
        return 1;
    }

    @Override
    public void sendPermissionError(@NotNull String hover) {
        this.sendMessage(ComponentText.of(ModConstants.translation("command.exception.permission")).style(style -> style.hoverEvent(HoverEvent.showText(ComponentText.of(hover)))));
    }

    @Override
    public void sendLangError(@NotNull String key, Object... objects) {
        this.sendError(ModConstants.translation(key, objects));
    }

    @Override
    public void sendMessage(net.minecraft.network.chat.Component text) {
        this.source.sendSuccess(text, false);
    }

    @Override
    public void sendMessage(@NotNull Component component) {
        this.sendMessage(ComponentText.toText(component));
    }

    @Override
    public void sendLangMessage(@NotNull String key, Object... objects) {
        this.sendMessage(ModConstants.translation(key, objects));
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public Vec3dLocation getLocationAsVector() throws CommandSyntaxException {
        return this.isConsole() ? null : Vec3dLocation.of(this.source.getPlayerOrException());
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
    public void setGameMode(GameType mode) {

    }

    @Override
    public boolean isConsole() {
        return CommandUtils.isConsole(this.source);
    }

    @Override
    public OnlineUser getUser() throws CommandSyntaxException {
        return KiloEssentials.getUserManager().getOnline(this.source);
    }

    @Override
    public CompoundTag toTag() {
        return null;
    }

    @Override
    public void fromTag(CompoundTag tag) {

    }

    @Override
    public String getName() {
        return this.source.getTextName();
    }

    @Override
    public UUID getId() {
        return null;
    }
}
