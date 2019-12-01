package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.client.network.packet.ChatMessageS2CPacket;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.ThreadManager;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.UserManager;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.threaded.ThreadedUserDateSaver;
import org.kilocraft.essentials.user.punishment.PunishmentManager;

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

    private PunishmentManager punishManager;

    public ServerUserManager(PlayerManager manager) {
        this.punishManager = new PunishmentManager(manager);
    }

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
        // TODO Impl Async checks later
        return CompletableFuture.completedFuture(new NeverJoinedUser());
    }

    @Override
    public CompletableFuture<User> getOffline(GameProfile profile) {
        profileSanityCheck(profile);
        return getOffline(profile.getId());
    }

    @Override
    public Map<UUID, OnlineServerUser> getOnlineUsers() {
        return onlineUsers;
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
    public void saveUser(OnlineServerUser user) throws IOException {
        this.userHandler.saveData(user);
    }

    @Override
    public void saveAllUsers() {
        ThreadManager saverThread = new ThreadManager(new ThreadedUserDateSaver(this));
        saverThread.start();
    }

    @Override
    public void onChangeNickname(User user, String oldNick) {
        this.nicknameToUUID.remove(oldNick);
        if(user.hasNickname()) {
            this.nicknameToUUID.put(user.getNickname().get(), user.getUuid());
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

        if(serverUser.hasNickname()) {
           this.nicknameToUUID.put(serverUser.getNickname().get(), playerEntity.getUuid());
        }


        KiloChat.broadcastUserJoinEventMessage(serverUser);
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
        KiloChat.broadcastUserLeaveEventMessage(user);
    }

    public void onChatMessage(ServerPlayerEntity player, ChatMessageC2SPacket packet) {
        if (player.getClientChatVisibility().equals(ChatVisibility.HIDDEN))
            player.networkHandler.sendPacket(new ChatMessageS2CPacket((new TranslatableText("chat.cannotSend")).formatted(Formatting.RED)));
        else {
            player.updateLastActionTime();
            String string = StringUtils.normalizeSpace(packet.getChatMessage());

            for(int i = 0; i < string.length(); ++i) {
                if (!SharedConstants.isValidChar(string.charAt(i))) {
                    player.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.illegal_characters"));
                    return;
                }
            }

            if (string.startsWith("/"))
                KiloEssentials.getInstance().getCommandHandler().execute(player.getCommandSource(), string);
            else
                ServerChat.sendChatMessage(player, string);

            ServerUser user = (ServerUser) KiloServer.getServer().getUserManager().getOnline(player);
            user.messageCooldown += 20;
            if (user.messageCooldown > 200 && !KiloEssentials.hasPermissionNode(player.getCommandSource(), "chat.spam")) {
                player.networkHandler.disconnect(new TranslatableText("disconnect.spam"));
            }
        }

    }

    public UserHandler getHandler() {
        return this.userHandler;
    }

    public PunishmentManager getPunishmentManager() {
        return this.punishManager;
    }

}
