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
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.util.Cached;
import org.kilocraft.essentials.chat.LangText;
import org.kilocraft.essentials.api.feature.TickListener;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.UserManager;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.setting.Settings;
import org.kilocraft.essentials.util.CacheManager;
import org.kilocraft.essentials.util.text.AnimatedText;
import org.kilocraft.essentials.util.SimpleProcess;
import org.kilocraft.essentials.util.player.UserUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ServerUserManager implements UserManager, TickListener {
    private static final Pattern COMPILE = Pattern.compile(".dat");
    private final UserHandler handler = new UserHandler();
    private final List<OnlineUser> users = new ArrayList<>();
    private final Map<String, UUID> nicknameToUUID = new HashMap<>();
    private final Map<String, UUID> usernameToUUID = new HashMap<>();
    private final Map<UUID, OnlineServerUser> onlineUsers = new HashMap<>();
    private final Map<UUID, Pair<Pair<UUID, Boolean>, Long>> teleportRequestsMap = new HashMap<>();
    private final Map<UUID, SimpleProcess<?>> inProcessUsers = new HashMap<>();
    private final String NICKNAME_CACHE = "nicknames";
    private List<String> cachedNicknames = new ArrayList<>();

    private PunishmentManager punishManager;

    public ServerUserManager(PlayerManager manager) {
        this.punishManager = new PunishmentManager(manager);
    }

    @Override
    public CompletableFuture<List<User>> getAll() {
        List<User> users = new ArrayList<>();

        for (File file : this.handler.getUserFiles()) {
            if (!file.exists()) {
                continue;
            }

            ServerUser serverUser = new ServerUser(UUID.fromString(COMPILE.matcher(file.getName()).replaceFirst("")));

            try {
                this.handler.loadUserAndResolveName(serverUser);

                if (serverUser.getUsername() != null) {
                    users.add(serverUser);
                }

            } catch (IOException ignored) {
            }
        }

        return CompletableFuture.completedFuture(users);
    }

    @Override
    public CompletableFuture<Optional<User>> getOffline(String username) {
        OnlineUser user = this.getOnlineNickname(username);
        if (user != null) {
            return CompletableFuture.completedFuture(Optional.of(user));
        }

        UUID ret = usernameToUUID.get(username);
        if (ret != null) {
            return getOffline(ret, username);
        }

        return this.getUserAsync(username);
    }

    private CompletableFuture<Optional<User>> getUserAsync(String username) {
        CompletableFuture<GameProfile> profileCompletableFuture = CompletableFuture.supplyAsync(() ->
                KiloServer.getServer().getVanillaServer().getUserCache().findByName(username)
        ); // This is hacky and probably doesn't work. //CODY_AI: But it works!

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
    public CompletableFuture<Optional<User>> getOffline(UUID uuid) {
        OnlineUser online = getOnline(uuid);
        if (online != null)
            return CompletableFuture.completedFuture(Optional.of(online));

        if (handler.userExists(uuid)) {
            ServerUser serverUser = new ServerUser(uuid).withCachedName();

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
        OnlineUser user = getOnline(usernameToUUID.get(username));
        return user == null ? getOnlineNickname(username) : user;
    }

    @Override
    @Nullable
    public OnlineUser getOnlineNickname(String nickname) {
        if (usernameToUUID.containsKey(nickname)) {
            return this.getOnline(nickname);
        }

        if (nicknameToUUID.containsKey(nickname)) {
            return this.getOnline(nicknameToUUID.get(nickname));
        }

        for (OnlineUser user : users) {
            if (user.hasNickname()) {
                String nick = org.kilocraft.essentials.api.util.StringUtils.stringToUsername(
                        TextFormat.clearColorCodes(user.getDisplayName()).replaceAll("\\s+", "")
                );

                if (nick.equals(nickname)) {
                    return user;
                }
            }
        }

        return null;
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

    public Map<UUID, Pair<Pair<UUID, Boolean>, Long>> getTeleportRequestsMap() {
        return this.teleportRequestsMap;
    }

    public Map<UUID, SimpleProcess<?>> getInProcessUsers() {
        return this.inProcessUsers;
    }

    @Override
    public void saveAllUsers() {
        if (SharedConstants.isDevelopment) {
            KiloEssentials.getLogger().info("Saving users data, this may take a while...");
        }

        for (OnlineServerUser user : onlineUsers.values()) {
            try {
                if (SharedConstants.isDevelopment) {
                    KiloEssentials.getLogger().info("Saving user \"{}\"", user.getUsername());
                }
                this.handler.save(user);
            } catch (IOException e) {
                KiloEssentials.getLogger().error("An unexpected exception occurred when saving a user's data!", e);
            }
        }

        if (SharedConstants.isDevelopment) {
            KiloEssentials.getLogger().info("Saved the users data!");
        }
    }

    @Override
    public void onChangeNickname(User user, String oldNick) {
        if (oldNick != null) {
            this.nicknameToUUID.remove(oldNick);
            this.cachedNicknames.remove(org.kilocraft.essentials.api.util.StringUtils.uniformNickname(oldNick));

            user.getNickname().ifPresent((nick) -> {
                this.nicknameToUUID.put(nick, user.getUuid());
                this.cachedNicknames.add(org.kilocraft.essentials.api.util.StringUtils.uniformNickname(nick));
            });
        }

        if (user.isOnline()) {
            KiloServer.getServer().getPlayerManager().sendToAll(
                    new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, ((OnlineUser) user).asPlayer()));
        }
    }

    public boolean shouldNotUseNickname(OnlineUser user, String rawNickname) {
        if (!CacheManager.shouldUse(NICKNAME_CACHE)) {
            List<String> nicks = new ArrayList<>();
            KiloEssentials.getInstance().getAllUsersThenAcceptAsync(user, "general.please_wait", (list) -> {
                for (User victim : list) {
                    victim.getNickname().ifPresent(nick -> {
                        nicks.add(org.kilocraft.essentials.api.util.StringUtils.uniformNickname(nick).toLowerCase(Locale.ROOT));
                    });
                }
            });

            cachedNicknames = nicks;
            Cached<List<String>> cached = new Cached<>(NICKNAME_CACHE, nicks);
            CacheManager.cache(cached);
        }

        boolean canUse = true;
        String uniformedNickname = org.kilocraft.essentials.api.util.StringUtils.uniformNickname(rawNickname).toLowerCase(Locale.ROOT);

        if (cachedNicknames.contains(uniformedNickname)) {
            canUse = false;
        }

        return !canUse;
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
    }

    public void onJoined(ServerPlayerEntity playerEntity) {
        OnlineServerUser user = (OnlineServerUser) this.getOnline(playerEntity);
        user.onJoined();
        KiloChat.onUserJoin(user);
    }

    public void onLeave(ServerPlayerEntity player) {
        OnlineServerUser user = this.onlineUsers.get(player.getUuid());
        user.onLeave();
        this.teleportRequestsMap.remove(user.getId());
        UserUtils.Process.remove(user);
        if (user.getNickname().isPresent()) {
            this.nicknameToUUID.remove(user.getNickname().get());
        }
        this.usernameToUUID.remove(player.getEntityName());
        this.users.remove(user);

        try {
            this.handler.save(user);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.onlineUsers.remove(player.getUuid());
        KiloChat.onUserLeave(user);
    }

    public void onChatMessage(OnlineUser user, ChatMessageC2SPacket packet) {
        ServerPlayerEntity player = user.asPlayer();
        NetworkThreadUtils.forceMainThread(packet, player.networkHandler, player.getServerWorld());

        player.updateLastActionTime();
        String string = StringUtils.normalizeSpace(packet.getChatMessage());

        for (int i = 0; i < string.length(); ++i) {
            if (!SharedConstants.isValidChar(string.charAt(i))) {
                if (KiloConfig.main().chat().kickForUsingIllegalCharacters) {
                    player.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.illegal_characters"));
                } else {
                    player.getCommandSource().sendError(new TranslatableText("multiplayer.disconnect.illegal_characters"));
                }

                return;
            }
        }

        ((OnlineServerUser) user).messageCooldown += 20;
        if (((ServerUser) user).messageCooldown > 200 && !user.hasPermission(EssentialPermission.CHAT_BYPASS)) {
            if (KiloConfig.main().chat().kickForSpamming) {
                player.networkHandler.disconnect(new TranslatableText("disconnect.spam"));
            } else {
                player.getCommandSource().sendError(LangText.getFormatter(true, "channel.spam"));
            }
        }

        if (string.startsWith("/")) {
            KiloEssentials.getInstance().getCommandHandler().execute(player.getCommandSource(), string);
        } else {
            ServerChat.sendSafely(user, new TextMessage(string), user.getSetting(Settings.CHAT_CHANNEL));
        }

    }

    @Override
    public void onTick() {
        for (OnlineUser user : users) {
            if (user == null) {
                continue;
            }

            ((OnlineServerUser) user).onTick();
        }
    }

    public UserHandler getHandler() {
        return this.handler;
    }

    public PunishmentManager getPunishmentManager() {
        return this.punishManager;
    }

    public static class LoadingText {
        private AnimatedText animatedText;
        public LoadingText(ServerPlayerEntity player) {
            this.animatedText = new AnimatedText(0, 315, TimeUnit.MILLISECONDS, player, TitleS2CPacket.Action.ACTIONBAR)
                    .append(LangText.get(true, "general.wait_server.frame1"))
                    .append(LangText.get(true, "general.wait_server.frame2"))
                    .append(LangText.get(true, "general.wait_server.frame3"))
                    .append(LangText.get(true, "general.wait_server.frame4"))
                    .build();
        }

        public LoadingText(ServerPlayerEntity player, String key) {
            this.animatedText = new AnimatedText(0, 315, TimeUnit.MILLISECONDS, player, TitleS2CPacket.Action.ACTIONBAR)
                    .append(LangText.get(true, key + ".frame1"))
                    .append(LangText.get(true, key + ".frame2"))
                    .append(LangText.get(true, key + ".frame3"))
                    .append(LangText.get(true, key + ".frame4"))
                    .build();
        }

        public LoadingText start() {
            this.animatedText.setStyle(new Style().setColor(Formatting.YELLOW)).start();
            return this;
        }

        public void stop() {
            this.animatedText.remove();
            this.animatedText = null;
        }
    }

    public void onServerReady() {
        if (KiloConfig.main().autoUserUpgrade) {
            this.handler.upgrade();
        }
    }

    public void appendCachedName(ServerUser user) {
        user.name = user.cachedName;
    }

}
