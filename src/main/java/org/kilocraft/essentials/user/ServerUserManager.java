package org.kilocraft.essentials.user;


import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.BanEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.feature.TickListener;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.*;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.user.punishment.PunishmentEntry;
import org.kilocraft.essentials.api.util.Cached;
import org.kilocraft.essentials.api.util.StringUtils;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.config.ConfigObjectReplacerUtil;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.ModerationConfigSection;
import org.kilocraft.essentials.events.PlayerEvents;
import org.kilocraft.essentials.mixin.accessor.ServerConfigEntryAccessor;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.*;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.text.AnimatedText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ServerUserManager implements UserManager, TickListener {
    private static final Pattern DAT_FILE_PATTERN = Pattern.compile(".dat");
    private static final Pattern USER_FILE_NAME = Pattern.compile(StringUtils.UUID_PATTERN + "\\.dat");
    private final UserHandler handler = new UserHandler();
    private final ServerPunishmentManager punishmentManager = new ServerPunishmentManager();
    private final List<OnlineUser> users = new ArrayList<>();
    private final Map<String, UUID> nicknameToUUID = new HashMap<>();
    private final Map<String, UUID> usernameToUUID = new HashMap<>();
    private final Map<UUID, OnlineServerUser> onlineUsers = new HashMap<>();
    private final Map<UUID, Pair<Pair<UUID, Boolean>, Long>> teleportRequestsMap = new HashMap<>();
    private final MutedPlayerList mutedPlayerList = new MutedPlayerList(new File(KiloEssentials.getDataDirPath() + "/mutes.json"));
    private Map<UUID, String> cachedNicknames = new HashMap<>();

    public ServerUserManager() {
        PlayerEvents.DEATH.register(player -> this.onDeath(this.getOnline(player)));
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

        UUID ret = this.usernameToUUID.get(username);
        if (ret != null) {
            return this.getOffline(ret, username);
        }

        return this.getUserAsync(username);
    }

    private CompletableFuture<Optional<User>> getUserAsync(String username) {
        CompletableFuture<GameProfile> profileCompletableFuture = CompletableFuture.supplyAsync(() ->
                KiloEssentials.getMinecraftServer().getUserCache().findByName(username).orElse(null)
        );

        return profileCompletableFuture.thenApplyAsync(profile -> this.getOffline(profile).join());
    }

    @Override
    public CompletableFuture<Optional<User>> getOffline(UUID uuid, String username) {
        OnlineUser online = this.getOnline(uuid);
        if (online != null)
            return CompletableFuture.completedFuture(Optional.of(online));

        if (this.handler.userExists(uuid)) {
            ServerUser serverUser = new ServerUser(uuid);
            serverUser.name = username;
            return CompletableFuture.completedFuture(Optional.of(serverUser));
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Optional<User>> getOffline(UUID uuid) {
        OnlineUser online = this.getOnline(uuid);
        if (online != null)
            return CompletableFuture.completedFuture(Optional.of(online));

        if (this.handler.userExists(uuid)) {
            ServerUser serverUser = new ServerUser(uuid).useSavedName();
            return CompletableFuture.completedFuture(Optional.of(serverUser));
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    @Nullable
    public CompletableFuture<Optional<User>> getOffline(GameProfile profile) {
        if (this.profileHasID(profile)) return this.getOffline(profile.getId(), profile.getName());
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public Map<UUID, OnlineServerUser> getOnlineUsers() {
        return this.onlineUsers;
    }

    @Override
    public List<OnlineUser> getOnlineUsersAsList() {
        return this.users;
    }

    @Override
    public List<OnlineUser> getOnlineUsersAsList(boolean includeVanished) {
        return this.users.stream().filter(onlineUser -> !onlineUser.getPreference(Preferences.VANISH) || includeVanished).collect(Collectors.toList());
    }

    @Override
    @Nullable
    public OnlineUser getOnline(GameProfile profile) {
        if (this.profileIsComplete(profile)) return this.getOnline(profile.getId());
        return null;
    }

    @Override
    @Nullable
    public OnlineUser getOnline(UUID uuid) {
        return this.onlineUsers.get(uuid);
    }

    @Override
    @Nullable
    public OnlineUser getOnline(String username) {
        OnlineUser user = this.getOnline(this.usernameToUUID.get(username));
        return user == null ? this.getOnlineNickname(username) : user;
    }

    @Override
    @Nullable
    public OnlineUser getOnlineNickname(String nickname) {
        if (this.usernameToUUID.containsKey(nickname)) {
            return this.getOnline(nickname);
        }

        if (this.nicknameToUUID.containsKey(nickname)) {
            return this.getOnline(this.nicknameToUUID.get(nickname));
        }

        for (OnlineUser user : this.users) {
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
        return this.getOnline(player.getUuid());
    }

    @Override
    public OnlineUser getOnline(ServerCommandSource source) throws CommandSyntaxException {
        return this.getOnline(source.getPlayer());
    }

    @Override
    public boolean isOnline(User user) {
        return this.onlineUsers.containsKey(user.getUuid());
    }

    public Map<UUID, Pair<Pair<UUID, Boolean>, Long>> getTeleportRequestsMap() {
        return this.teleportRequestsMap;
    }

    @Override
    public void saveAllUsers() {
        for (OnlineServerUser user : this.onlineUsers.values()) {
            try {
                this.handler.save(user);
            } catch (IOException e) {
                KiloEssentials.getLogger().fatal("An unexpected exception occurred when saving a user's data!", e);
            }
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

        if (user instanceof OnlineUser onlineUser) {
            PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, onlineUser.asPlayer());
            KiloEssentials.getInstance().sendGlobalPacket(packet);
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
            this.getAllUsersThenAcceptAsync(user, "general.please_wait", (list) -> {
                for (User victim : list) {
                    victim.getNickname().ifPresent(nick -> map.put(
                            victim.getUuid(),
                            org.kilocraft.essentials.api.util.StringUtils.uniformNickname(nick).toLowerCase(Locale.ROOT)
                    ));

                    map.put(victim.getUuid(), org.kilocraft.essentials.api.util.StringUtils.uniformNickname(victim.getUsername()).toLowerCase(Locale.ROOT));
                }
            });

            this.cachedNicknames = map;
            Cached<Map<UUID, String>> cached = new Cached<>(NICKNAME_CACHE, map);
            CacheManager.cache(cached);
        }

        AtomicBoolean canUse = new AtomicBoolean(true);
        String uniformedNickname = org.kilocraft.essentials.api.util.StringUtils.uniformNickname(rawNickname).toLowerCase(Locale.ROOT);

        for (Map.Entry<UUID, String> entry : this.cachedNicknames.entrySet()) {
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
        List<GameProfile> banned = new ArrayList<>();
        for (BannedPlayerEntry bannedPlayerEntry : KiloEssentials.getMinecraftServer().getPlayerManager().getUserBanList().values()) {
            GameProfile profile = ((ServerConfigEntryAccessor<GameProfile>) bannedPlayerEntry).getKey();
            Optional<User> optional = this.getOffline(profile).join();
            optional.ifPresent(player -> {
                if (Objects.equals(player.getLastIp(), user.getLastIp())) banned.add(profile);
            });
        }
        List<OnlineUser> online = new ArrayList<>();
        for (Map.Entry<UUID, OnlineServerUser> entry : this.onlineUsers.entrySet()) {
            if (entry.getValue().getLastIp().equals(user.getLastIp())) {
                online.add(entry.getValue());
            }
        }
        if (!banned.isEmpty() || online.size() > 1) {
            TextComponent.Builder builder = Component.text();
            for (GameProfile profile : banned) {
                builder.append(Component.text("[" + profile.getName() + "] ").color(NamedTextColor.RED));
            }
            for (OnlineUser onlineUser : online) {
                builder.append(Component.text("[" + onlineUser.getName() + "] ").color(NamedTextColor.GREEN));
            }
            ServerChat.Channel.STAFF.send(ComponentText.toText(builder.build()));
        }
    }

    public void onLeave(ServerPlayerEntity player) {
        OnlineServerUser user = this.onlineUsers.get(player.getUuid());
        user.onLeave();
        this.teleportRequestsMap.remove(user.getId());
        if (user.getNickname().isPresent()) {
            this.nicknameToUUID.remove(user.getNickname().get());
        }
        this.usernameToUUID.remove(player.getEntityName());
        this.users.remove(user);

        try {
            this.handler.save(user);
        } catch (IOException e) {
            KiloEssentials.getLogger().fatal("Failed to Save User Data [" + player.getEntityName() + "/" + player.getUuidAsString() + "]", e);
        }

        this.onlineUsers.remove(player.getUuid());
    }

    public void onChatMessage(ServerPlayerEntity player, TextStream.Message textStream) {
        OnlineUser user = this.getOnline(player);
        if (this.punishmentManager.isMuted(user.getUuid())) {
            user.sendMessage(getMuteMessage(user));
        } else {
            ServerChat.sendChatMessage(user, Format.validatePermission(user, textStream.getRaw(), EssentialPermission.PERMISSION_PREFIX + "chat.formatting"), user.getPreference(Preferences.CHAT_CHANNEL));
        }
    }

    @Override
    public void onTick() {
        for (OnlineUser user : this.users) {
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
    }

    public UserHandler getHandler() {
        return this.handler;
    }

    public void onServerReady() {
        if (KiloConfig.main().autoUserUpgrade) {
            this.handler.upgrade();
        }
    }

    public final CompletableFuture<List<User>> getAllUsersThenAcceptAsync(final OnlineUser requester,
                                                                          final String loadingTitle,
                                                                          final Consumer<? super List<User>> action) {
        CommandSourceUser src = CommandSourceServerUser.of(requester.getCommandSource());
        final LoadingText loadingText = new LoadingText(requester.asPlayer(), loadingTitle);

        if (!src.isConsole()) {
            loadingText.start();
        }

        final CompletableFuture<List<User>> future = this.getAll();
        future.thenAcceptAsync(list -> {
            if (!src.isConsole()) {
                loadingText.stop();
            }

            try {
                action.accept(list);
            } catch (Exception e) {
                requester.sendError(e.getMessage());
            }
        });

        return future;
    }


    public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final ServerCommandSource requester,
                                                                    final String username,
                                                                    final Consumer<? super User> action) {
        if (CommandUtils.isOnline(requester)) {
            return this.getUserThenAcceptAsync(this.getOnline(requester.getName()), username, action);
        }

        final CompletableFuture<Optional<User>> optionalCompletableFuture = this.getOffline(username);
        optionalCompletableFuture.thenAcceptAsync(optionalUser -> {
            if (!optionalUser.isPresent() || optionalUser.get() instanceof NeverJoinedUser) {
                CommandSourceServerUser.of(requester).sendLangError("exception.user_not_found");
                return;
            }

            try {
                optionalUser.ifPresent(action);
            } catch (Exception e) {
                requester.sendError(new LiteralText(e.getMessage()).formatted(Formatting.RED));
            }
        }, KiloEssentials.getMinecraftServer());

        return optionalCompletableFuture;
    }


    public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final ServerPlayerEntity requester,
                                                                    final String username,
                                                                    final Consumer<? super User> action) {
        return this.getUserThenAcceptAsync(KiloEssentials.getUserManager().getOnline(requester), username, action);
    }


    public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final OnlineUser requester,
                                                                    final String username,
                                                                    final Consumer<? super User> action) {
        final CompletableFuture<Optional<User>> optionalCompletableFuture = this.getOffline(username);
        final LoadingText loadingText = new LoadingText(requester.asPlayer());
        optionalCompletableFuture.thenAcceptAsync(optionalUser -> {
            loadingText.stop();

            if (!optionalUser.isPresent() || optionalUser.get() instanceof NeverJoinedUser) {
                requester.sendLangError("exception.user_not_found");
                return;
            }

            try {
                action.accept(optionalUser.get());
            } catch (Exception e) {
                requester.sendError(e.getMessage());
            }
        }, KiloEssentials.getMinecraftServer());

        if (!optionalCompletableFuture.isDone())
            loadingText.start();

        return optionalCompletableFuture;
    }


    public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final OnlineUser requester,
                                                                    final UUID uuid,
                                                                    final Consumer<? super User> action) {

        final CompletableFuture<Optional<User>> optionalCompletableFuture = this.getOffline(uuid);
        final LoadingText loadingText = new LoadingText(requester.asPlayer());
        optionalCompletableFuture.thenAcceptAsync(optionalUser -> {
            loadingText.stop();

            if (optionalUser.isEmpty()) {
                requester.sendLangError("exception.user_not_found");
                return;
            }

            try {
                action.accept(optionalUser.get());
            } catch (Exception e) {
                requester.sendError(e.getMessage());
            }
        }, KiloEssentials.getMinecraftServer());

        if (!optionalCompletableFuture.isDone())
            loadingText.start();

        return optionalCompletableFuture;
    }


    public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final String username,
                                                                    final Consumer<? super Optional<User>> action) {
        if (this.getOnline(username) != null) {
            return CompletableFuture.completedFuture(Optional.ofNullable(this.getOnline(username)));
        }

        final CompletableFuture<Optional<User>> optionalCompletableFuture = this.getOffline(username);
        optionalCompletableFuture.thenAcceptAsync(action);
        return optionalCompletableFuture;
    }


    public CompletableFuture<Optional<User>> getUserThenAcceptAsync(UUID uuid, Consumer<? super Optional<User>> action) {
        User user = this.getOnline(uuid);
        if (user != null) {
            Optional<User> optionalUser = Optional.of(user);
            action.accept(optionalUser);
            return CompletableFuture.completedFuture(optionalUser);
        }

        final CompletableFuture<Optional<User>> optionalCompletableFuture = this.getOffline(uuid);
        optionalCompletableFuture.thenAcceptAsync(action);
        return optionalCompletableFuture;
    }


    public Optional<User> getUserThenAccept(UUID uuid, Consumer<? super Optional<User>> action) {
        User user = this.getOnline(uuid);
        if (user != null) {
            Optional<User> optionalUser = Optional.of(user);
            action.accept(optionalUser);
            return optionalUser;
        }

        final CompletableFuture<Optional<User>> optionalCompletableFuture = this.getOffline(uuid);
        try {
            action.accept(optionalCompletableFuture.get());
        } catch (InterruptedException | ExecutionException ignored) {
            action.accept(Optional.empty());
        }

        return Optional.empty();
    }


    public Optional<User> getUser(UUID uuid) {
        return this.getOffline(uuid).join();
    }


    public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final String username, final Consumer<? super Optional<User>> action, final Executor executor) {
        if (this.getOnline(username) != null) {
            return CompletableFuture.completedFuture(Optional.ofNullable(this.getOnline(username)));
        }

        final CompletableFuture<Optional<User>> optionalCompletableFuture = this.getOffline(username);
        optionalCompletableFuture.thenAcceptAsync(action, executor);
        return optionalCompletableFuture;
    }

    public static class LoadingText {
        private AnimatedText animatedText;

        public LoadingText(ServerPlayerEntity player) {
            this(player, "general.wait_server");
        }

        public LoadingText(ServerPlayerEntity player, String key) {
            this.animatedText = new AnimatedText(0, 315, TimeUnit.MILLISECONDS, player)
                    .append(StringText.of(key + ".frame1"))
                    .append(StringText.of(key + ".frame2"))
                    .append(StringText.of(key + ".frame3"))
                    .append(StringText.of(key + ".frame4"))
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

    public static String replaceBanVariables(final String str, final BanEntry<?> entry, final boolean permanent) {
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
        MutedPlayerEntry entry = KiloEssentials.getUserManager().getMutedPlayerList().get(user.asPlayer().getGameProfile());
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
