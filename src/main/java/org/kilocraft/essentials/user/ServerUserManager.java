package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.UserManager;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.AnimatedText;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ServerUserManager implements UserManager {
    private static final Pattern COMPILE = Pattern.compile(".dat");
    private final UserHandler handler = new UserHandler();
    private final List<OnlineUser> users = new ArrayList<>();
    private final Map<String, UUID> nicknameToUUID = new HashMap<>();
    private final Map<String, UUID> usernameToUUID = new HashMap<>();
    private final Map<UUID, OnlineServerUser> onlineUsers = new HashMap<>();

    private PunishmentManager punishManager;

    public ServerUserManager(PlayerManager manager) {
        this.punishManager = new PunishmentManager(manager);
    }

    @Override
    public CompletableFuture<List<User>> getAll() {
        final List<User> users = new ArrayList<>();

        for (final File file : this.handler.getUserFiles()) {
            if (!file.exists()) {
                continue;
            }

            ServerUser serverUser = new ServerUser(UUID.fromString(COMPILE.matcher(file.getName()).replaceFirst("")));

            try {
                this.handler.loadUserAndResolveName(serverUser);

                if (serverUser.getUsername() != null) {
                    users.add(serverUser);
                }

            } catch (final IOException ignored) {
            }
        }

        return CompletableFuture.completedFuture(users);
    }

    @Override
    public CompletableFuture<Optional<User>> getOffline(String username) {
        UUID ret = usernameToUUID.get(username);
        if (ret != null) {
            return getOffline(ret, username);
        }

        return this.getUserAsync(username);
    }

    private CompletableFuture<Optional<User>> getUserAsync(String username) {
        CompletableFuture<GameProfile> profileCompletableFuture = CompletableFuture.supplyAsync(() -> {
            GameProfile profile = KiloServer.getServer().getVanillaServer().getUserCache().findByName(username);

            return profile;
        }); // This is hacky and probably doesn't work. //CODY_AI: But it works!

        return profileCompletableFuture.thenApplyAsync(profile -> this.getOffline(profile).join());
    }

    @Override
    public CompletableFuture<Optional<User>> getOffline(UUID uuid, String username) {
        OnlineUser online = getOnline(uuid);
        if (online != null)
            return CompletableFuture.completedFuture(Optional.of(online));

        if (handler.userExists(uuid)) {
            ServerUser serverUser = new ServerUser(uuid);
            serverUser.name = username;

            return CompletableFuture.completedFuture(Optional.of(serverUser));
        }

        return CompletableFuture.completedFuture(Optional.of(new NeverJoinedUser()));
    }

    @Override
    public CompletableFuture<Optional<User>> getOffline(GameProfile profile) {
        profileSanityCheck(profile);
        return getOffline(profile.getId(), profile.getName());
    }

    @Override
    public Map<UUID, OnlineServerUser> getOnlineUsers() {
        return onlineUsers;
    }

    @Override
    public List<OnlineUser> getOnlineUsersAsList() {
        return users;
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
                this.handler.saveData(serverUser);
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
        if (oldNick != null) {
            this.nicknameToUUID.remove(oldNick);
            user.getNickname().ifPresent((nick) -> this.nicknameToUUID.put(nick, user.getUuid()));
        }

        if (user.isOnline()) {
            KiloServer.getServer().getPlayerManager().sendToAll(
                    new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, ((OnlineUser) user).getPlayer()));
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
        this.users.add(serverUser);

        serverUser.getNickname().ifPresent((nick) -> this.nicknameToUUID.put(nick, playerEntity.getUuid()));

        KiloServer.getServer().getChatManager().getChannel("global").join(serverUser);
        KiloChat.onUserJoin(serverUser);
    }

    public void onLeave(ServerPlayerEntity player) {
        OnlineServerUser user = this.onlineUsers.get(player.getUuid());
        KiloServer.getServer().getChatManager().getChannel(GlobalChat.getChannelId()).leave(user);
        if (user.getNickname().isPresent())
            this.nicknameToUUID.remove(user.getNickname().get());
        this.usernameToUUID.remove(player.getEntityName());
        this.users.remove(user);

        try {
            this.handler.saveData(user);
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

        for (int i = 0; i < string.length(); ++i) {
            if (!SharedConstants.isValidChar(string.charAt(i))) {
                if (KiloConfig.main().chat().kickForUsingIllegalCharacters)
                    player.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.illegal_characters"));
                else
                    player.getCommandSource().sendError(new TranslatableText("multiplayer.disconnect.illegal_characters"));

                return;
            }
        }

        if (string.startsWith("/")) {
            KiloEssentials.getInstance().getCommandHandler().execute(player.getCommandSource(), string);
        } else {
            KiloServer.getServer().getChatManager().onChatMessage(player, packet);
        }

        ServerUser user = (ServerUser) KiloServer.getServer().getUserManager().getOnline(player);

        //user.messageCooldown += 20;
        if (user.messageCooldown > 200 && !KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.CHAT_BYPASS)) {
            player.networkHandler.disconnect(new TranslatableText("disconnect.spam"));
        }

    }

    public void onTick() {
        for (OnlineUser user : users) {
            if (user == null)
                continue;

            ((OnlineServerUser) user).onTick();
        }
    }

    public UserHandler getHandler() {
        return this.handler;
    }

    public PunishmentManager getPunishmentManager() {
        return this.punishManager;
    }

    public static class UserLoadingText {
        private AnimatedText animatedText;
        public UserLoadingText(ServerPlayerEntity player) {
            this.animatedText = new AnimatedText(0, 650, TimeUnit.MILLISECONDS, player, TitleS2CPacket.Action.ACTIONBAR)
                    .append(LangText.get(true, "general.wait_server.frame1"))
                    .append(LangText.get(true, "general.wait_server.frame2"))
                    .append(LangText.get(true, "general.wait_server.frame3"))
                    .append(LangText.get(true, "general.wait_server.frame4"))
                    .build();
        }

        public UserLoadingText(ServerPlayerEntity player, String key) {
            this.animatedText = new AnimatedText(0, 100, TimeUnit.MILLISECONDS, player, TitleS2CPacket.Action.ACTIONBAR)
                    .append(LangText.get(true, key + ".frame1"))
                    .append(LangText.get(true, key + ".frame2"))
                    .append(LangText.get(true, key + ".frame3"))
                    .append(LangText.get(true, key + ".frame4"))
                    .build();
        }

        public UserLoadingText start() {
            this.animatedText.setStyle(new Style().setColor(Formatting.YELLOW)).start();
            return this;
        }

        public void stop() {
            this.animatedText.remove();
            this.animatedText = null;
        }
    }

    public void onServerReady() {
//        if (KiloConfig.main().autoUserUpgrade) {
//            this.handler.upgrade();
//        }
    }

}
