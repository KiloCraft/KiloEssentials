package org.kilocraft.essentials.user;


import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.BanEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.Format;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.feature.TickListener;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.PunishmentManager;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.UserManager;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.user.punishment.PunishmentEntry;
import org.kilocraft.essentials.api.util.Cached;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.config.ConfigObjectReplacerUtil;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.ModerationConfigSection;
import org.kilocraft.essentials.events.player.PlayerClientCommandEventImpl;
import org.kilocraft.essentials.events.player.PlayerMutedEventImpl;
import org.kilocraft.essentials.extensions.betterchairs.SeatManager;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.*;
import org.kilocraft.essentials.util.player.UserUtils;
import org.kilocraft.essentials.util.text.AnimatedText;
import org.kilocraft.essentials.util.text.Texter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class ServerUserManager implements UserManager, TickListener {
    private static final Pattern DAT_FILE_PATTERN = Pattern.compile(".dat");
    private static final Pattern USER_FILE_NAME = Pattern.compile(org.kilocraft.essentials.api.util.StringUtils.UUID_PATTERN + "\\.dat");
    private final UserHandler handler = new UserHandler();
    private final ServerPunishmentManager punishmentManager = new ServerPunishmentManager();
    private final List<OnlineUser> users = new ArrayList<>();
    private final Map<String, UUID> nicknameToUUID = new HashMap<>();
    private final Map<String, UUID> usernameToUUID = new HashMap<>();
    private final Map<UUID, OnlineServerUser> onlineUsers = new HashMap<>();
    private final Map<UUID, Pair<Pair<UUID, Boolean>, Long>> teleportRequestsMap = new HashMap<>();
    private final Map<UUID, SimpleProcess<?>> inProcessUsers = new HashMap<>();
    private final MutedPlayerList mutedPlayerList = new MutedPlayerList(new File(KiloEssentials.getDataDirPath() + "/mutes.json"));
    private Map<UUID, String> cachedNicknames = new HashMap<>();

    public ServerUserManager() {
    }

    @Override
    public CompletableFuture<List<User>> getAll() {
        List<User> users = new ArrayList<>();

        for (File file : this.handler.getUserFiles()) {
            if (!file.exists() || !USER_FILE_NAME.matcher(file.getName()).matches()) {
                continue;
            }

            try {
                ServerUser user = new ServerUser(UUID.fromString(DAT_FILE_PATTERN.matcher(file.getName()).replaceFirst("")));
                this.handler.loadUserAndResolveName(user);

                if (user.getUsername() != null) {
                    users.add(user);
                }

            } catch (Exception e) {
                KiloEssentials.getLogger().error("Can not load the user file \"{}\"!", file.getName(), e);
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
                KiloServer.getServer().getMinecraftServer().getUserCache().findByName(username)
        );

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

        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Optional<User>> getOffline(UUID uuid) {
        OnlineUser online = getOnline(uuid);
        if (online != null)
            return CompletableFuture.completedFuture(Optional.of(online));

        if (handler.userExists(uuid)) {
            ServerUser serverUser = new ServerUser(uuid).useSavedName();
            return CompletableFuture.completedFuture(Optional.of(serverUser));
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    @Nullable
    public CompletableFuture<Optional<User>> getOffline(GameProfile profile) {
        if (profileHasID(profile)) return getOffline(profile.getId(), profile.getName());
        return CompletableFuture.completedFuture(Optional.empty());
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
        if (profileIsComplete(profile)) return getOnline(profile.getId());
        return null;
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
                        ComponentText.clearFormatting(user.getDisplayName()).replaceAll("\\s+", "")
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
            KiloDebugUtils.getLogger().info("Saving users data, this may take a while...");
        }

        for (OnlineServerUser user : onlineUsers.values()) {
            try {
                if (SharedConstants.isDevelopment) {
                    KiloDebugUtils.getLogger().info("Saving user \"{}\"", user.getUsername());
                }
                this.handler.save(user);
            } catch (IOException e) {
                KiloEssentials.getLogger().fatal("An unexpected exception occurred when saving a user's data!", e);
            }
        }

        if (SharedConstants.isDevelopment) {
            KiloDebugUtils.getLogger().info("Saved the users data!");
        }
    }

    @Override
    public void onChangeNickname(User user, String oldNick) {
        if (oldNick != null) {
            this.nicknameToUUID.remove(oldNick);
            this.cachedNicknames.remove(user.getUuid());

            user.getNickname().ifPresent((nick) -> {
                this.nicknameToUUID.put(nick, user.getUuid());
                this.cachedNicknames.put(user.getUuid(), org.kilocraft.essentials.api.util.StringUtils.uniformNickname(nick));
            });
        }

        if (user.isOnline()) {
            KiloServer.getServer().getMetaManager().updateDisplayName(((OnlineUser) user).asPlayer());
        }
    }

    @Override
    public PunishmentManager getPunishmentManager() {
        return this.punishmentManager;
    }

    @Override
    public MutedPlayerList getMutedPlayerList() {
        return this.mutedPlayerList;
    }

    @Override
    public void onPunishmentPerformed(OnlineUser src, PunishmentEntry entry, Punishment.Type type, @Nullable String expiry, boolean silent) {
        final ModerationConfigSection config = KiloConfig.main().moderation();
        assert entry.getVictim() != null;
        final String message = config.meta().performed
                .replace("{TYPE}", type == Punishment.Type.MUTE ? config.meta().wordMuted : config.meta().wordBanned)
                .replace("{SOURCE}", src.getName())
                .replace("{VICTIM}", entry.getVictim() == null ? entry.getVictimIP() == null ? "INVALID" : entry.getVictimIP() : entry.getVictim().getName())
                .replace("{REASON}", entry.getReason() == null ? (type == Punishment.Type.MUTE ? config.defaults().mute : config.defaults().ban) : entry.getReason())
                .replace("{LENGTH}", expiry == null ? config.meta().wordPermanent : expiry);


            if (silent) {
            ServerChat.Channel.STAFF.send(ComponentText.toText(config.meta().silentPrefix + " " + message));
        } else if (config.meta().broadcast) {
            KiloChat.broadCast(message);
        }
    }

    @Override
    public void onPunishmentRevoked(OnlineUser src, PunishmentEntry entry, Punishment.Type type, @Nullable String expiry, boolean silent) {
        final ModerationConfigSection config = KiloConfig.main().moderation();
        assert entry.getVictim() != null;
        final String message = config.meta().revoked
                .replace("{TYPE}", type == Punishment.Type.MUTE ? config.meta().wordMuted : config.meta().wordBanned)
                .replace("{SOURCE}", src.getName())
                .replace("{VICTIM}", entry.getVictim() == null ? entry.getVictimIP() == null ? "INVALID" : entry.getVictimIP() : entry.getVictim().getName())
                .replace("{REASON}", entry.getReason() == null ? config.defaults().ban : entry.getReason())
                .replace("{LENGTH}", expiry == null ? config.meta().wordPermanent : expiry);

        if (silent) {
            ServerChat.Channel.STAFF.send(ComponentText.toText(config.meta().silentPrefix + " " + message));
        } else if (config.meta().broadcast) {
            KiloChat.broadCast(message);
        }
    }

    public boolean shouldNotUseNickname(OnlineUser user, String rawNickname) {
        String NICKNAME_CACHE = "nicknames";
        if (!CacheManager.isPresent(NICKNAME_CACHE)) {
            Map<UUID, String> map = new HashMap<>();
            KiloEssentials.getInstance().getAllUsersThenAcceptAsync(user, "general.please_wait", (list) -> {
                for (User victim : list) {
                    victim.getNickname().ifPresent(nick -> map.put(
                            victim.getUuid(),
                            org.kilocraft.essentials.api.util.StringUtils.uniformNickname(nick).toLowerCase(Locale.ROOT)
                    ));

                    map.put(victim.getUuid(), org.kilocraft.essentials.api.util.StringUtils.uniformNickname(victim.getUsername()).toLowerCase(Locale.ROOT));
                }
            });

            cachedNicknames = map;
            Cached<Map<UUID, String>> cached = new Cached<>(NICKNAME_CACHE, map);
            CacheManager.cache(cached);
        }

        AtomicBoolean canUse = new AtomicBoolean(true);
        String uniformedNickname = org.kilocraft.essentials.api.util.StringUtils.uniformNickname(rawNickname).toLowerCase(Locale.ROOT);

        for (Map.Entry<UUID, String> entry : cachedNicknames.entrySet()) {
            UUID uuid = entry.getKey();
            String string = entry.getValue();
            if (string.equalsIgnoreCase(uniformedNickname) && !user.getUuid().equals(uuid)) {
                canUse.set(false);
                break;
            }
        }

        return !canUse.get();
    }

    private boolean profileIsComplete(GameProfile profile) {
        return profile != null && profile.isComplete();
    }

    private boolean profileHasID(GameProfile profile) {
        return profile != null && profile.getId() != null;
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
        KiloChat.onUserLeave(user);
        user.onLeave();
        this.teleportRequestsMap.remove(user.getId());
        if (user.getNickname().isPresent()) {
            this.nicknameToUUID.remove(user.getNickname().get());
        }
        this.usernameToUUID.remove(player.getEntityName());
        this.users.remove(user);

        if (UserUtils.Process.isInAny(user)) {
            UserUtils.Process.remove(user);
        }

        try {
            this.handler.save(user);
        } catch (IOException e) {
            KiloEssentials.getLogger().fatal("Failed to Save User Data [" + player.getEntityName() + "/" + player.getUuidAsString() + "]", e);
        }

        this.onlineUsers.remove(player.getUuid());
    }

    public void onChatMessage(OnlineUser user, ChatMessageC2SPacket packet) {
        ServerPlayerEntity player = user.asPlayer();
        NetworkThreadUtils.forceMainThread(packet, player.networkHandler, player.getServerWorld());

        String string = StringUtils.normalizeSpace(packet.getChatMessage());
        player.updateLastActionTime();

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

        ((OnlineServerUser) user).messageCoolDown += 20;
        if (((ServerUser) user).messageCoolDown > 200 && !user.hasPermission(EssentialPermission.CHAT_BYPASS)) {
            if (KiloConfig.main().chat().kickForSpamming) {
                player.networkHandler.disconnect(new TranslatableText("disconnect.spam"));
            } else {
                if (((ServerUser) user).systemMessageCoolDown > 400) {
                    user.sendMessage(KiloConfig.main().chat().spamWarning);
                }
            }

            return;
        }

        try {
            if (string.startsWith("/")) {
                KiloEssentials.getInstance().getCommandHandler().execute(player.getCommandSource(), string);
            } else {
                if (punishmentManager.isMuted(user)) {
                    user.sendMessage(getMuteMessage(user));
                    return;
                }
                string = Format.parse(user, string, PermissionUtil.PERMISSION_PREFIX + "chat.formatting.");
                ServerChat.sendChatMessage(user, string, user.getPreference(Preferences.CHAT_CHANNEL));
            }
        } catch (Exception e) {
            MutableText text = Texter.newTranslatable("command.failed");
            if (SharedConstants.isDevelopment) {
                text.append("\n").append(Util.getInnermostMessage(e));
                KiloDebugUtils.getLogger().error("Processing a chat message throw an exception", e);
            }

            user.getCommandSource().sendError(text);
        }

    }

    @Override
    public void onTick() {
        for (OnlineUser user : users) {
            if (user == null) {
                continue;
            }

            try {
                ((OnlineServerUser) user).onTick();
            } catch (Exception e) {
                KiloEssentials.getLogger().fatal("DEBUG: ServerUserManager.onTick() -> user.onTick()", e);
            }
        }
    }

    public void onDeath(OnlineUser user) {
        user.saveLocation();

        if (SeatManager.isEnabled() && SeatManager.getInstance().isSitting(user.asPlayer())) {
            SeatManager.getInstance().unseat(user);
        }
    }

    public UserHandler getHandler() {
        return this.handler;
    }

    public void onServerReady() {
        if (KiloConfig.main().autoUserUpgrade) {
            this.handler.upgrade();
        }
    }

    public void appendCachedName(ServerUser user) {
        user.name = user.savedName;
    }

    public static class LoadingText {
        private AnimatedText animatedText;

        public LoadingText(ServerPlayerEntity player) {
            this.animatedText = new AnimatedText(0, 315, TimeUnit.MILLISECONDS, player, TitleS2CPacket.Action.ACTIONBAR)
                    .append(StringText.of(true, "general.wait_server.frame1"))
                    .append(StringText.of(true, "general.wait_server.frame2"))
                    .append(StringText.of(true, "general.wait_server.frame3"))
                    .append(StringText.of(true, "general.wait_server.frame4"))
                    .build();
        }

        public LoadingText(ServerPlayerEntity player, String key) {
            this.animatedText = new AnimatedText(0, 315, TimeUnit.MILLISECONDS, player, TitleS2CPacket.Action.ACTIONBAR)
                    .append(StringText.of(true, key + ".frame1"))
                    .append(StringText.of(true, key + ".frame2"))
                    .append(StringText.of(true, key + ".frame3"))
                    .append(StringText.of(true, key + ".frame4"))
                    .build();
        }

        public LoadingText start() {
            this.animatedText.setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW)).start();
            return this;
        }

        public void stop() {
            this.animatedText.remove();
            this.animatedText = null;
        }
    }

    public static String replaceVariables(final String str, final BanEntry<?> entry, final boolean permanent) {
        ConfigObjectReplacerUtil replacer = new ConfigObjectReplacerUtil("ban", str, true)
                .append("reason", entry.getReason())
                .append("source", entry.getSource());

        if (!permanent) {
            SimpleDateFormat dateFormat = ModConstants.DATE_FORMAT;
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            replacer.append("expiry", dateFormat.format(entry.getExpiryDate()))
                    .append("left", TimeDifferenceUtil.formatDateDiff(new Date(), entry.getExpiryDate()));
        }

        return replacer.toString();
    }

    public static String getMuteMessage(final OnlineUser user) {
        MutedPlayerEntry entry = KiloServer.getServer().getUserManager().getMutedPlayerList().get(user.asPlayer().getGameProfile());
        assert entry != null;

        if (entry.getExpiryDate() == null) {
            return KiloConfig.main().moderation().messages().mute
                    .replace("{MUTE_REASON}", entry.getReason() == null ? KiloConfig.main().moderation().defaults().mute : entry.getReason());
        } else {
            return KiloConfig.main().moderation().messages().tempMute
                    .replace("{MUTE_REASON}", entry.getReason() == null ? KiloConfig.main().moderation().defaults().mute : entry.getReason())
                    .replace("{MUTE_LEFT}", TimeDifferenceUtil.formatDateDiff(new Date(), entry.getExpiryDate()));
        }
    }

}
