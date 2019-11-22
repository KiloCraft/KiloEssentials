package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.UserManager;

import java.io.File;
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
    public CompletableFuture<User> getOffline(String username) {
        UUID ret = usernameToUUID.get(username);
        if(ret != null) {
            return getOffline(ret);
        }

        return this.getUserAsync(username);
    }

    private CompletableFuture<User> getUserAsync(String username) {
        CompletableFuture<GameProfile> profileCompletableFuture = CompletableFuture.supplyAsync(() -> {
            GameProfile profile = KiloServer.getServer().getVanillaServer().getUserCache().findByName(username);

            if(profile == null) {
                return null;
            }

            return profile;
        }); // This is hacky and probably doesn't work.

        return profileCompletableFuture.thenApplyAsync(profile -> this.getOffline(profile).join());
    }

    @Override
    public CompletableFuture<User> getOffline(UUID uuid) {
        OnlineUser online = getOnline(uuid);
        if(online != null) {
            return CompletableFuture.completedFuture(online);
        }

        // TODO. Check the username history page, if the username is not present than dump the NeverJoined out.

        // TODO if the username is present, then pull an async call to read and form the user, then return the user within the future.

        File target = this.getHandler().getUserFile(uuid);
        if(!target.exists()) {
            return CompletableFuture.completedFuture(new NeverJoinedUser());
        }

        ServerUser user = new ServerUser(uuid);

        return CompletableFuture.completedFuture(user);
    }

    @Override
    public CompletableFuture<User> getOffline(GameProfile profile) {
        profileSanityCheck(profile);
        return getOffline(profile.getId());
    }

    @Override
    @Nullable
    public OnlineUser getOnline(GameProfile profile) {
        profileSanityCheck(profile);
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
