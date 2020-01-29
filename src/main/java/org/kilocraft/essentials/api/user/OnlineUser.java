package org.kilocraft.essentials.api.user;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.extensions.betterchairs.PlayerSitManager;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

public interface OnlineUser extends User {
    ServerPlayerEntity getPlayer();

    ServerCommandSource getCommandSource();

    void teleport(Location loc, boolean sendTicket);

    void sendMessage(String message);

    int sendError(String message);

    int sendError(ExceptionMessageNode node);

    void sendMessage(Text text);

    void sendMessage(ChatMessage chatMessage);

    void sendLangMessage(String key, Object... objects);

    void sendConfigMessage(String key, Object... objects);

    Vec3dLocation getLocationAsVector();

    void setSittingType(PlayerSitManager.SummonType type);

    @Nullable
    PlayerSitManager.SummonType getSittingType();
}
