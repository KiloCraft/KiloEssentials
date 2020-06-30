package org.kilocraft.essentials.api.user;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.text.OnlineMessageReceptionist;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

public interface OnlineUser extends User, OnlineMessageReceptionist {
    ServerPlayerEntity asPlayer();

    ServerCommandSource getCommandSource();

    void sendSystemMessage(Object sysMessage);

    void teleport(@NotNull final Location loc, boolean sendTicket);

    void teleport(@NotNull final OnlineUser user);

    int sendError(ExceptionMessageNode node, Object... objects);

    void sendConfigMessage(String key, Object... objects);

    ClientConnection getConnection();

    Vec3dLocation getLocationAsVector();

    Vec3d getEyeLocation();

    boolean hasPermission(CommandPermission perm);

    boolean hasPermission(EssentialPermission perm);

    void setFlight(boolean set);

    void setGameMode(GameMode mode);

}
