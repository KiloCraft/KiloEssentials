package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.UserManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServerUserManager implements UserManager {
    private UserHandler userHandler = new UserHandler();
    private Map<String, UUID> nicknameToUUID = new HashMap<>();
    private Map<String, UUID> usernameToUUID = new HashMap<>();
    private Map<UUID, OnlineServerUser> onlineUsers = new HashMap<>();

    @Override
    public CompletableFuture<User> getOffline(UUID uuid) {
        OnlineUser online = getOnline(uuid);
        if(online != null) {
            return CompletableFuture.completedFuture(online);
        }

        // TODO Read the User files, otherwise create a dummy user instance.
        return null;
    }

    @Override
    public CompletableFuture<User> getOffline(GameProfile profile) {
        profileSanityCheck(profile);
        return getOffline(profile.getId());
    }

    @Override
    @Nullable
    public OnlineUser getOnline(GameProfile profile) {
        if(!profile.isComplete()) {

        }

        return getOnline(profile.getId());
    }

    @Override
    @Nullable
    public OnlineUser getOnline(UUID uuid) {
        return onlineUsers.get(uuid);
    }

    @Override
    @Nullable
    public OnlineUser getOnline(String username) {
        return getOnline(usernameToUUID.get(username));
    }

    @Override
    @Nullable
    public OnlineUser getOnlineNickname(String nickname) {
        return getOnline(nicknameToUUID.get(nickname));
    }

    @Override
    public OnlineUser getOnline(ServerPlayerEntity player) {
        return getOnline(player.getUuid());
    }

    @Override
    public OnlineUser getOnline(ServerCommandSource source) throws CommandSyntaxException {
        return getOnline(source.getPlayer());
    }

    @Override
    public void saveAll() {
        for (OnlineServerUser serverUser : onlineUsers.values()) {
            try {
                this.getHandler().saveData(serverUser);
            } catch (IOException e) {
                e.printStackTrace(); // TODO how did this fail
            }
        }
    }

    private void profileSanityCheck(GameProfile profile) {
        if(!profile.isComplete() && profile.getId() == null) {
            throw new IllegalArgumentException("Cannot use GameProfile with missing username to get an OfflineUser");
        }
    }

    public void onJoin(ServerPlayerEntity playerEntity) {
        OnlineServerUser serverUser = new OnlineServerUser(playerEntity);

        this.onlineUsers.put(playerEntity.getUuid(), serverUser);
        this.usernameToUUID.put(playerEntity.getGameProfile().getName(), playerEntity.getUuid());

        if(serverUser.getNickname() != null && serverUser.getNickname() != "") {
           this.nicknameToUUID.put(serverUser.getNickname(), playerEntity.getUuid());
        }


        //KiloChat.broadcastUserJoinEventMessage(user);
    }

    public void onLeave(ServerPlayerEntity player) {
        OnlineServerUser user = this.onlineUsers.get(player.getUuid());
        this.nicknameToUUID.remove(user.getNickname());
        this.usernameToUUID.remove(player.getEntityName());

        try {
            getHandler().saveData(user);
        } catch (IOException e) {
            e.printStackTrace(); // TODO how did this fail?
        }

        this.onlineUsers.remove(player.getUuid());
        //KiloChat.broadcastUserLeaveEventMessage(user);
    }

    public UserHandler getHandler() {
        return this.userHandler;
    }
}
