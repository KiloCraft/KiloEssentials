package org.kilocraft.essentials.api.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.text.OnlineMessageReceptionist;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.EssentialPermission;

import java.text.SimpleDateFormat;
import java.util.Date;

public interface OnlineUser extends User, OnlineMessageReceptionist {
    ServerPlayer asPlayer();

    CommandSourceStack getCommandSource();

    void sendSystemMessage(Object sysMessage);

    void teleport(@NotNull final Location loc, boolean sendTicket);

    void teleport(@NotNull final OnlineUser user);

    Connection getConnection();

    Vec3dLocation getLocationAsVector() throws CommandSyntaxException;

    boolean hasPermission(CommandPermission perm);

    boolean hasPermission(EssentialPermission perm);

    void setFlight(boolean set);

    void setGameMode(GameType mode);

    default Component hoverEvent() {
        String date = String.format(ModConstants.translation("channel.message.hover.time"), new SimpleDateFormat(ModConstants.translation("channel.message.hover.time_format")).format(new Date()));
        if (this.hasNickname()) {
            return ComponentText.of(ModConstants.translation("channel.message.hover.nicked", this.getUsername(), date));
        } else {
            return ComponentText.of(ModConstants.translation("channel.message.hover", date));
        }
    }

}
