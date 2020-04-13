package org.kilocraft.essentials.api.user;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

public interface OnlineUser extends User {
    ServerPlayerEntity asPlayer();

    ServerCommandSource getCommandSource();

    void teleport(Location loc, boolean sendTicket);

    void sendMessage(String message);

    int sendError(String message);

    int sendError(Text text);

    int sendLangError(String key, Object... objects);

    int sendError(ExceptionMessageNode node, Object... objects);

    void sendMessage(Text text);

    void sendMessage(TextMessage textMessage);

    void sendLangMessage(String key, Object... objects);

    void sendConfigMessage(String key, Object... objects);

    ClientConnection getConnection();

    Vec3dLocation getLocationAsVector();

    Vec3d getEyeLocation();

    boolean hasPermission(CommandPermission perm);

    boolean hasPermission(EssentialPermission perm);

    void setFlight(boolean set);

    void setGameMode(GameMode mode);

}
