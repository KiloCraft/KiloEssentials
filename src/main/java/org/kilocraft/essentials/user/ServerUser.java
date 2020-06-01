package org.kilocraft.essentials.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.text.MessageReceptionist;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.api.user.settting.UserSettings;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.UserMessageReceptionist;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.setting.ServerUserSettings;
import org.kilocraft.essentials.user.setting.Settings;
import org.kilocraft.essentials.util.nbt.NBTUtils;
import org.kilocraft.essentials.util.player.UserUtils;
import org.kilocraft.essentials.util.text.Texter;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * Main User Implementation
 *
 * @author CODY_AI (OnBlock)
 * @see User
 * @see ServerUserManager
 * @see UserHomeHandler
 * @see UserSettings
 * @see OnlineUser
 * @see org.kilocraft.essentials.api.user.CommandSourceUser
 * @see org.kilocraft.essentials.user.UserHandler
 * @see net.minecraft.entity.player.PlayerEntity
 * @see net.minecraft.server.network.ServerPlayerEntity
 * @since 1.5
 */

public class ServerUser implements User {
    public static final int SYS_MESSAGE_COOL_DOWN = 400;
    protected static final ServerUserManager MANAGER = (ServerUserManager) KiloServer.getServer().getUserManager();
    private final ServerUserSettings settings;
    private UserHomeHandler homeHandler;
    private Vec3dLocation lastLocation;
    private boolean hasJoinedBefore = true;
    private Date firstJoin = new Date();
    public int messageCoolDown;
    public int systemMessageCoolDown;
    private MessageReceptionist lastDmReceptionist;
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
        this.settings = new ServerUserSettings();

        if (UserHomeHandler.isEnabled()) {
            this.homeHandler = new UserHomeHandler(this);
        }

        try {
            MANAGER.getHandler().handleUser(this);
        } catch (IOException e) {
            KiloEssentials.getLogger().fatal("Failed to Load User Data [" + uuid.toString() + "]", e);
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

    public void fromTag(@NotNull CompoundTag compoundTag) {
        CompoundTag metaTag = compoundTag.getCompound("meta");
        CompoundTag cacheTag = compoundTag.getCompound("cache");

        if (cacheTag.contains("lastLoc")) {
            this.lastLocation = Vec3dLocation.dummy();
            this.lastLocation.fromTag(cacheTag.getCompound("cIp"));
        }

        if (compoundTag.contains("loc")) {
            this.location = Vec3dLocation.dummy();
            this.location.fromTag(compoundTag.getCompound("loc"));
            this.location.shortDecimals();
        }

        if (cacheTag.contains("cIp")) {
            this.lastSocketAddress = cacheTag.getString("ip");
        }

        if (cacheTag.contains("dmRec")) {
            CompoundTag lastDmTag = cacheTag.getCompound("dmRec");
            this.lastDmReceptionist = new UserMessageReceptionist(lastDmTag.getString("name"), NBTUtils.getUUID(lastDmTag, "id"));
        }

        this.firstJoin = dateFromString(metaTag.getString("firstJoin"));
        this.lastOnline = dateFromString(metaTag.getString("lastOnline"));
        this.hasJoinedBefore = metaTag.getBoolean("hasJoinedBefore");

        if (metaTag.contains("ticksPlayed")) {
            this.ticksPlayed = metaTag.getInt("ticksPlayed");
        }

        if (metaTag.contains("isStaff")) {
            this.isStaff = true;
        }

        if (UserHomeHandler.isEnabled()) {
            this.homeHandler.deserialize(compoundTag.getCompound("homes"));
        }

        this.savedName = compoundTag.getString("name");
        this.settings.fromTag(compoundTag.getCompound("settings"));
    }

    public void updateLocation() {
        if (this.isOnline() && ((OnlineUser) this).asPlayer().getPos() != null) {
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
        return this instanceof OnlineUser || MANAGER.isOnline(this);
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
        return TextFormat.translate(this.getDisplayName() + TextFormat.RESET.toString());
    }

    @Override
    public Text getRankedDisplayName() {
        if (this.isOnline()) {
            return UserUtils.getDisplayNameWithMeta((OnlineUser) this, true);
        }

        return Texter.newText(this.getDisplayName());
    }

    @Override
    public Text getRankedName() {
        if (this.isOnline()) {
            return UserUtils.getDisplayNameWithMeta((OnlineUser) this, false);
        }

        return Texter.newText(this.name);
    }

    @Override
    public String getNameTag() {
        String str = this.isOnline() ? KiloConfig.messages().general().userTags().online :
                KiloConfig.messages().general().userTags().offline;
        return str.replace("{USER_NAME}", this.name)
                .replace("{USER_DISPLAYNAME}", this.getFormattedDisplayName());
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
    public UserSettings getSettings() {
        return this.settings;
    }

    @Override
    public <T> T getSetting(Setting<T> setting) {
        return this.settings.get(setting);
    }

    @Override
    public Optional<String> getNickname() {
        return this.getSetting(Settings.NICK);
    }

    @Override
    public Location getLocation() {
        if (this.isOnline() || (this.isOnline() && this.location == null)) {
            updateLocation();
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
        this.getSettings().set(Settings.NICK, Optional.of(name));
        KiloServer.getServer().getUserManager().onChangeNickname(this, this.getNickname().isPresent() ? this.getNickname().get() : ""); // This is to update the entries in UserManager.
    }

    @Override
    public void clearNickname() {
        KiloServer.getServer().getUserManager().onChangeNickname(this, null); // This is to update the entries in UserManager.
        this.getSettings().reset(Settings.NICK);
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
    public <F extends UserProvidedFeature> F feature(FeatureType<F> type) {
        return null; // TODO Impl
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
            throw new SimpleCommandExceptionType(new LiteralText(e.getMessage()).formatted(Formatting.RED)).create();
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
    public boolean ignored(UUID uuid) {
        return this.getSetting(Settings.IGNORE_LIST).containsValue(uuid);
    }

    @Override
    public MessageReceptionist getLastMessageReceptionist() {
        return this.lastDmReceptionist;
    }

    @Override
    public void setLastMessageReceptionist(MessageReceptionist receptionist) {
        this.lastDmReceptionist = receptionist;
    }

    public static void saveLocationOf(ServerPlayerEntity player) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);

        if (user != null) {
            user.saveLocation();
        }
    }

    public boolean shouldMessage() {
        return !this.getSetting(Settings.DON_NOT_DISTURB);
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