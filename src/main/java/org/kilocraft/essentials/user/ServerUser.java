package org.kilocraft.essentials.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.text.Format;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.preference.Preference;
import org.kilocraft.essentials.api.user.preference.UserPreferences;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.api.util.StringUtils;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.user.preference.ServerUserPreferences;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.nbt.NBTUtils;
import org.kilocraft.essentials.util.player.UserUtils;
import org.kilocraft.essentials.util.text.Texter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * Main User Implementation
 *
 * @author CODY_AI (OnBlock)
 * @see User
 * @see ServerUserManager
 * @see UserHomeHandler
 * @see UserPreferences
 * @see OnlineUser
 * @see org.kilocraft.essentials.api.user.CommandSourceUser
 * @see org.kilocraft.essentials.user.UserHandler
 * @see net.minecraft.world.entity.player.Player
 * @see net.minecraft.server.level.ServerPlayer
 * @since 1.5
 */

public class ServerUser implements User {
    public static final int SYS_MESSAGE_COOL_DOWN = 400;
    protected static final ServerUserManager MANAGER = KiloEssentials.getUserManager();
    private final ServerUserPreferences settings;
    private UserHomeHandler homeHandler;
    private Vec3dLocation lastLocation;
    private boolean hasJoinedBefore = true;
    private Date firstJoin = new Date();
    public int messageCoolDown;
    public int systemMessageCoolDown;
    private EntityIdentifiable lastDmReceptionist;
    final UUID uuid;
    String name = "";
    String savedName = "";
    Vec3dLocation location;
    boolean isStaff = false;
    String lastSocketAddress;
    int ticksPlayed = 0;
    Date lastOnline;

    public ServerUser(@NotNull final UUID uuid) {
        this.uuid = uuid;
        this.settings = new ServerUserPreferences();

        if (UserHomeHandler.isEnabled()) {
            this.homeHandler = new UserHomeHandler(this);
        }

        try {
            MANAGER.getHandler().handleUser(this);
        } catch (IOException e) {
            KiloEssentials.getLogger().fatal("Failed to Load User Data [" + uuid + "]", e);
        }

    }

    public CompoundTag toTag() {
        CompoundTag mainTag = new CompoundTag();
        CompoundTag metaTag = new CompoundTag();
        CompoundTag cacheTag = new CompoundTag();

        // Here we store the players current location
        if (this.location != null) {
            this.location.shortDecimals();
            mainTag.put("loc", this.location.toTag());
        }

        if (this.lastLocation != null) {
            cacheTag.put("cLoc", this.lastLocation.toTag());
        }

        if (this.lastSocketAddress != null) {
            cacheTag.putString("ip", this.lastSocketAddress);
        }

        metaTag.putString("firstJoin", ModConstants.DATE_FORMAT.format(this.firstJoin));
        if (this.lastOnline != null) {
            metaTag.putString("lastOnline", ModConstants.DATE_FORMAT.format(this.lastOnline));
        }

        if (this.ticksPlayed != -1) {
            metaTag.putInt("ticksPlayed", this.ticksPlayed);
        }

        if (this.isStaff) {
            metaTag.putBoolean("isStaff", true);
        }

        if (UserHomeHandler.isEnabled() || this.homeHandler != null) {
            CompoundTag homeTag = new CompoundTag();
            this.homeHandler.serialize(homeTag);
            mainTag.put("homes", homeTag);
        }

        mainTag.put("meta", metaTag);
        mainTag.put("cache", cacheTag);
        mainTag.put("settings", this.settings.toTag());
        mainTag.putString("name", this.name);
        return mainTag;
    }

    public void fromTag(@NotNull CompoundTag NbtCompound) {
        CompoundTag metaTag = NbtCompound.getCompound("meta");
        CompoundTag cacheTag = NbtCompound.getCompound("cache");

        if (cacheTag.contains("lastLoc")) {
            this.lastLocation = Vec3dLocation.dummy();
            this.lastLocation.fromTag(cacheTag.getCompound("lastLoc"));
        }

        if (NbtCompound.contains("loc")) {
            this.location = Vec3dLocation.dummy();
            this.location.fromTag(NbtCompound.getCompound("loc"));
            this.location.shortDecimals();
        }

        if (cacheTag.contains("ip")) {
            this.lastSocketAddress = cacheTag.getString("ip");
        }


        if (cacheTag.contains("dmRec")) {
            CompoundTag lastDmTag = cacheTag.getCompound("dmRec");
            this.lastDmReceptionist = new EntityIdentifiable() {
                @Override
                public UUID getId() {
                    return NBTUtils.getUUID(lastDmTag, "id");
                }

                @Override
                public String getName() {
                    return lastDmTag.getString("name");
                }
            };
        }

        this.firstJoin = this.dateFromString(metaTag.getString("firstJoin"));
        this.lastOnline = this.dateFromString(metaTag.getString("lastOnline"));
        this.hasJoinedBefore = metaTag.getBoolean("hasJoinedBefore");

        if (metaTag.contains("ticksPlayed")) {
            this.ticksPlayed = metaTag.getInt("ticksPlayed");
        }

        if (metaTag.contains("isStaff")) {
            this.isStaff = true;
        }

        if (UserHomeHandler.isEnabled()) {
            this.homeHandler.deserialize(NbtCompound.getCompound("homes"));
        }

        this.savedName = NbtCompound.getString("name");
        if (cacheTag.contains("IIP")) {
            this.lastSocketAddress = cacheTag.getString("IIP");
            KiloEssentials.getLogger().info("Updating ip for " + this.savedName);
        }
        this.settings.fromTag(NbtCompound.getCompound("settings"));
    }

    public void updateLocation() {
        if (this.isOnline() && ((OnlineUser) this).asPlayer().position() != null) {
            this.location = Vec3dLocation.of(((OnlineUser) this).asPlayer()).shortDecimals();
        }
    }

    private Date dateFromString(String stringToParse) {
        Date date = new Date();
        try {
            date = ModConstants.DATE_FORMAT.parse(stringToParse);
        } catch (ParseException ignored) {
            this.hasJoinedBefore = false;
        }
        return date;
    }

    @Nullable
    public UserHomeHandler getHomesHandler() {
        return this.homeHandler;
    }

    @Nullable
    @Override
    public String getLastSocketAddress() {
        return this.lastSocketAddress;
    }

    @Nullable
    @Override
    public String getLastIp() {
        return StringUtils.socketAddressToIp(this.lastSocketAddress);
    }

    @Override
    public int getTicksPlayed() {
        return this.ticksPlayed;
    }

    @Override
    public void setTicksPlayed(int ticks) {
        this.ticksPlayed = ticks;
    }

    @Override
    public boolean isOnline() {
        return MANAGER.isOnline(this);
    }

    @Override
    public boolean hasNickname() {
        return this.getNickname().isPresent();
    }

    public String getDisplayName() {
        return this.getNickname().orElseGet(() -> this.name);
    }

    @Override
    public String getFormattedDisplayName() {
        return ComponentText.translateOld(this.getDisplayName() + Format.RESET);
    }

    @Override
    public Component getRankedDisplayName() {
        if (this.isOnline()) {
            return UserUtils.getDisplayNameWithMeta((OnlineUser) this, true);
        }

        return Texter.newText(this.getDisplayName());
    }

    @Override
    public String getRankedDisplayNameAsString() {
        try {
            if (this.isOnline()) {
                return UserUtils.getDisplayNameWithMetaAsString((OnlineUser) this, true);
            }
        } catch (IllegalStateException ignored) {
        }
        return this.getDisplayName();
    }

    @Override
    public Component getRankedName() {
        if (this.isOnline()) {
            return UserUtils.getDisplayNameWithMeta((OnlineUser) this, false);
        }

        return Texter.newText(this.name);
    }

    @Override
    public String getNameTag() {
        return ModConstants.translation(this.isOnline() ? "player.tag.online" : "player.tag.offline", this.getFormattedDisplayName());
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public UserPreferences getPreferences() {
        return this.settings;
    }

    @Override
    public <T> T getPreference(Preference<T> preference) {
        return this.settings.get(preference);
    }

    @Override
    public Optional<String> getNickname() {
        Optional<String> optional = this.getPreference(Preferences.NICK);
        return optional.map(s -> s + "<reset>").or(() -> optional);
    }

    @Override
    public Location getLocation() {
        if (this.isOnline() || (this.isOnline() && this.location == null)) {
            this.updateLocation();
        }

        return this.location;
    }

    @Nullable
    @Override
    public Location getLastSavedLocation() {
        return this.lastLocation;
    }

    @Override
    public void saveLocation() {
        if (this instanceof OnlineUser)
            this.lastLocation = Vec3dLocation.of((OnlineUser) this).shortDecimals();
    }

    @Override
    public void setNickname(String name) {
        this.getPreferences().set(Preferences.NICK, Optional.of(name));
        KiloEssentials.getUserManager().onChangeNickname(this, this.getNickname().isPresent() ? this.getNickname().get() : ""); // This is to update the entries in UserManager.
    }

    @Override
    public void clearNickname() {
        KiloEssentials.getUserManager().onChangeNickname(this, null); // This is to update the entries in UserManager.
        this.getPreferences().reset(Preferences.NICK);
    }

    @Override
    public void setLastLocation(Location loc) {
        this.lastLocation = (Vec3dLocation) loc;
    }

    @Override
    public boolean hasJoinedBefore() {
        return this.hasJoinedBefore;
    }

    @Override
    public Date getFirstJoin() {
        return this.firstJoin;
    }

    @Override
    public @Nullable Date getLastOnline() {
        return this.lastOnline;
    }

    @Override
    public void saveData() throws IOException {
        if (!this.isOnline())
            MANAGER.getHandler().save(this);
    }

    @Override
    public void trySave() throws CommandSyntaxException {
        if (this.isOnline())
            return;

        try {
            this.saveData();
        } catch (IOException e) {
            throw new SimpleCommandExceptionType(new TextComponent(e.getMessage()).withStyle(ChatFormatting.RED)).create();
        }
    }

    public boolean isStaff() {
        if (this.isOnline())
            this.isStaff = KiloEssentials.hasPermissionNode(((OnlineUser) this).getCommandSource(), EssentialPermission.STAFF);

        return this.isStaff;
    }

    @Override
    public boolean equals(User anotherUser) {
        return anotherUser == this || anotherUser.getUuid().equals(this.uuid) || anotherUser.getUsername().equals(this.getUsername());
    }

    @Override
    public EntityIdentifiable getLastMessageReceptionist() {
        return this.lastDmReceptionist;
    }

    @Override
    public void setLastMessageReceptionist(EntityIdentifiable entity) {
        this.lastDmReceptionist = entity;
    }

    public static void saveLocationOf(ServerPlayer player) {
        OnlineUser user = KiloEssentials.getUserManager().getOnline(player);

        if (user != null) {
            user.saveLocation();
        }
    }

    public boolean shouldMessage() {
        return !this.getPreference(Preferences.DON_NOT_DISTURB);
    }

    public ServerUser useSavedName() {
        this.name = this.savedName;
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getId() {
        return this.uuid;
    }
}