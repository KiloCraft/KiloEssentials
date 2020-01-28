package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.SharedConstants;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.UserManager;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.ConfigCache;
import org.kilocraft.essentials.config.KiloConfig;
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
        if (ret != null) {
            return getOffline(ret, username);
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
    public CompletableFuture<User> getOffline(UUID uuid, String username) {
        OnlineUser online = getOnline(uuid);
        if (online != null)
            return CompletableFuture.completedFuture(online);

        if (userHandler.userExists(uuid)) {
            ServerUser serverUser = new ServerUser(uuid);
            serverUser.name = username;

            return CompletableFuture.completedFuture(serverUser);
        }

        // TODO Impl Async checks later
        return CompletableFuture.completedFuture(new NeverJoinedUser());
    }

    @Override
    public CompletableFuture<User> getOffline(GameProfile profile) {
        profileSanityCheck(profile);
        return getOffline(profile.getId(), profile.getName());
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
    public boolean isOnline(User user) {
        return this.onlineUsers.containsKey(user.getUuid());
    }

    @Override
    public void saveAllUsers() {
        if (SharedConstants.isDevelopment)
            KiloEssentials.getLogger().info("Saving users data, this may take a while...");

        for (OnlineServerUser serverUser : onlineUsers.values()) {
            try {
                if (SharedConstants.isDevelopment)
                    KiloEssentials.getLogger().debug("Saving user \"" + serverUser.getUsername() + "\"");
                this.userHandler.saveData(serverUser);
            } catch (IOException e) {
                KiloEssentials.getLogger().error("An unexpected exception occurred when saving a user's data!");
                e.printStackTrace();
            }
        }

        if (SharedConstants.isDevelopment)
            KiloEssentials.getLogger().info("Saved the users data!");
    }

    @Override
    public void onChangeNickname(User user, String oldNick) {
        this.nicknameToUUID.remove(oldNick);
        if (user.hasNickname()) {
            this.nicknameToUUID.put(user.getNickname().get(), user.getUuid());
        }
    }

    private void profileSanityCheck(GameProfile profile) {
        if (!profile.isComplete() && profile.getId() == null) {
            throw new IllegalArgumentException("Cannot use GameProfile with missing username to get an OfflineUser");
        }
    }

    public void onJoin(ServerPlayerEntity playerEntity) {
        OnlineServerUser serverUser = new OnlineServerUser(playerEntity);

        this.onlineUsers.put(playerEntity.getUuid(), serverUser);
        this.usernameToUUID.put(playerEntity.getGameProfile().getName(), playerEntity.getUuid());

        if (serverUser.getNickname().isPresent()) {
           this.nicknameToUUID.put(serverUser.getNickname().get(), playerEntity.getUuid());
        }

        KiloServer.getServer().getChatManager().getChannel("global").join(serverUser);
        KiloChat.onUserJoin(serverUser);
    }

    public void onLeave(ServerPlayerEntity player) {
        OnlineServerUser user = this.onlineUsers.get(player.getUuid());
        KiloServer.getServer().getChatManager().getChannel("global").leave(user);
        if (user.getNickname().isPresent())
            this.nicknameToUUID.remove(user.getNickname().get());
        this.usernameToUUID.remove(player.getEntityName());

        try {
            this.userHandler.saveData(user);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.onlineUsers.remove(player.getUuid());

        KiloChat.onUserLeave(user);
    }

    public void onChatMessage(ServerPlayerEntity player, ChatMessageC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, player.networkHandler, player.getServerWorld());

        player.updateLastActionTime();
        String string = StringUtils.normalizeSpace(packet.getChatMessage());

        for(int i = 0; i < string.length(); ++i) {
            if (!SharedConstants.isValidChar(string.charAt(i))) {
                if (KiloConfig.getProvider().getMain().getBooleanSafely(ConfigCache.KICK_IF_ILLEGAL_CHARACTERS, false))
                    player.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.illegal_characters"));
                else
                    player.getCommandSource().sendError(new TranslatableText("multiplayer.disconnect.illegal_characters"));

                return;
            }
        }

        if (string.startsWith("/"))
            KiloEssentials.getInstance().getCommandHandler().execute(player.getCommandSource(), string);
        else
            KiloServer.getServer().getChatManager().onChatMessage(player, packet);

        ServerUser user = (ServerUser) KiloServer.getServer().getUserManager().getOnline(player);

        if (user.messageCooldown > 1000 && !KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.CHAT_BYPASS)) {
            player.networkHandler.disconnect(new TranslatableText("disconnect.spam"));
        }

    }

    public void onTick() {
        for (ServerPlayerEntity playerEntity : KiloServer.getServer().getPlayerManager().getPlayerList()) {
            if (playerEntity == null)
                continue;

            ((ServerUser) getOnline(playerEntity)).resetMessageCooldown();
            ((ServerUser) getOnline(playerEntity)).updateLocation();
        }
    }

    public UserHandler getHandler() {
        return this.userHandler;
    }

    public PunishmentManager getPunishmentManager() {
        return this.punishManager;
    }

    public static SimpleCommandExceptionType TOO_MANY_PROFILES = new SimpleCommandExceptionType(new LiteralText("Only one user is allowed but the provided selector includes more!"));
}
