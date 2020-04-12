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
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.inventory.UserInventory;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.api.user.settting.UserSettings;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.inventory.ServerUserInventory;
import org.kilocraft.essentials.user.setting.ServerUserSettings;
import org.kilocraft.essentials.user.setting.Settings;
import org.kilocraft.essentials.util.NBTTypes;
import org.kilocraft.essentials.util.UserUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * @author CODY_AI
 * An easy way to handle the User (Instance of player)
 *
 * @see ServerUserManager
 * @see UserHomeHandler
 */

public class ServerUser implements User {
    protected static ServerUserManager manager = (ServerUserManager) KiloServer.getServer().getUserManager();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    UUID uuid;
    String name = "";
    String cachedName = "";
    private ServerUserSettings settings;
    private UserHomeHandler homeHandler;
    private UserInventory inventory;
    private Vec3dLocation location;
    private Vec3dLocation lastLocation;
    private UUID lastPrivateMessageGetterUUID;
    private String lastPrivateMessageText = "";
    private boolean hasJoinedBefore = true;
    private Date firstJoin = new Date();
    public int messageCooldown;
    boolean isStaff = false;
    String lastSocketAddress;
    int ticksPlayed = 0;

    public ServerUser(UUID uuid) {
        this(uuid, null);
    }

    public ServerUser(final UUID uuid, final @Nullable ServerPlayerEntity player) {
        this.uuid = uuid;
        this.settings = new ServerUserSettings();

        if (UserHomeHandler.isEnabled()) {
            this.homeHandler = new UserHomeHandler(this);
        }

        this.inventory = player == null ? new ServerUserInventory(this) : new ServerUserInventory(this, player);

        try {
            manager.getHandler().handleUser(this);
        } catch (IOException e) {
            KiloEssentials.getLogger().error("Failed to Load User Data [" + uuid.toString() + "]");
        }

    }

    protected CompoundTag serialize() {
        CompoundTag mainTag = new CompoundTag();
        CompoundTag metaTag = new CompoundTag();
        CompoundTag cacheTag = new CompoundTag();

        // Here we store the players current location
        if (this.location != null) {
            mainTag.put("loc", this.location.toTag());
            this.location.shortDecimals();
        }

        if (this.lastLocation != null)
            cacheTag.put("lastLoc", this.lastLocation.toTag());

        // Private messaging stuff
        if (this.getLastPrivateMessageSender() != null) {
            CompoundTag lastMessageTag = new CompoundTag();
            lastMessageTag.putString("destUUID", this.getLastPrivateMessageSender().toString());
            if (this.getLastPrivateMessage() != null) {
                lastMessageTag.putString("text", this.getLastPrivateMessage());
            }
            cacheTag.put("lastMessage", lastMessageTag);
        }

        if (this.lastSocketAddress != null) {
            cacheTag.putString("lIP", this.lastSocketAddress);
        }

        metaTag.putBoolean("hasJoinedBefore", this.hasJoinedBefore);
        metaTag.putString("firstJoin", dateFormat.format(this.firstJoin));

        if (this.ticksPlayed != -1)
            metaTag.putInt("ticksPlayed", this.ticksPlayed);

        if (this.isStaff)
            metaTag.putBoolean("isStaff", true);

        if (UserHomeHandler.isEnabled()) {
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

    protected void deserialize(@NotNull CompoundTag compoundTag) {
    	CompoundTag metaTag = compoundTag.getCompound("meta");
        CompoundTag cacheTag = compoundTag.getCompound("cache");

        if (cacheTag.contains("lastLoc")) {
            this.lastLocation = Vec3dLocation.dummy();
            this.lastLocation.fromTag(cacheTag.getCompound("lastLoc"));
        }

        if (compoundTag.contains("loc")) {
        	this.location = Vec3dLocation.dummy();
        	this.location.fromTag(compoundTag.getCompound("loc"));
        	this.location.shortDecimals();
        }

        if (cacheTag.contains("lastMessage", NBTTypes.COMPOUND)) {
            CompoundTag lastMessageTag = cacheTag.getCompound("lastMessage");
            if (lastMessageTag.contains("destUUID", NBTTypes.STRING))
                this.lastPrivateMessageGetterUUID = UUID.fromString(lastMessageTag.getString("destUUID"));

            if (lastMessageTag.contains("text", NBTTypes.STRING))
                this.lastPrivateMessageText = lastMessageTag.getString("text");

        }

        if (cacheTag.contains("lIP")) {
            this.lastSocketAddress = cacheTag.getString("lIP");
        }

        this.hasJoinedBefore = metaTag.getBoolean("hasJoinedBefore");
        this.firstJoin = getUserFirstJoinDate(metaTag.getString("firstJoin"));

        if (metaTag.contains("ticksPlayed"))
            this.ticksPlayed = metaTag.getInt("ticksPlayed");

        if (metaTag.contains("isStaff"))
            this.isStaff = true;

        if (UserHomeHandler.isEnabled()) {
            this.homeHandler.deserialize(compoundTag.getCompound("homes"));
        }

        this.cachedName = compoundTag.getString("name");
        this.settings.fromTag(compoundTag.getCompound("settings"));
    }

    public void updateLocation() {
        if (this instanceof OnlineUser && ((OnlineUser) this).getPlayer().getPos() != null) {
            this.location = Vec3dLocation.of((OnlineUser) this).shortDecimals();
        }
    }

    private Date getUserFirstJoinDate(String stringToParse) {
        Date date = new Date();
        try {
            date = dateFormat.parse(stringToParse);
        } catch (ParseException e) {
            //Pass, this is the first time that user is joined.
        }
        return date;
    }

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
        return this instanceof OnlineUser || KiloServer.getServer().getUserManager().isOnline(this);
    }

    @Override
    public boolean hasNickname() {
        return this.getNickname().isPresent();
    }

    public String getDisplayName() {
        Optional<String> nickname = this.getSetting(Settings.NICKNAME);
        return nickname.orElseGet(() -> this.name);
    }

    @Override
    public String getFormattedDisplayName() {
        return TextFormat.translate(getDisplayName() + "&r");
    }

    @Override
    public Text getRankedDisplayName() {
        return UserUtils.getDisplayNameWithMeta(this, true);
    }

    @Override
    public Text getRankedName() {
        return UserUtils.getDisplayNameWithMeta(this, false);
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
        return this.getSetting(Settings.NICKNAME);
    }

    @Override
    public Location getLocation() {
        if (this instanceof OnlineUser && this.location == null && ((OnlineUser) this).getPlayer() != null)
            updateLocation();

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
        this.getSettings().set(Settings.NICKNAME, Optional.of(name));
        KiloServer.getServer().getUserManager().onChangeNickname(this, this.getNickname().isPresent() ? this.getNickname().get() : ""); // This is to update the entries in UserManager.
    }

    @Override
    public void clearNickname() {
        KiloServer.getServer().getUserManager().onChangeNickname(this, null); // This is to update the entries in UserManager.
        this.getSettings().set(Settings.NICKNAME, Optional.empty());
    }

    @Override
    public void setLastLocation(Location loc) {
        this.lastLocation = (Vec3dLocation) loc;
    }

    @Override
    public UUID getLastPrivateMessageSender() {
        return this.lastPrivateMessageGetterUUID;
    }

    @Override
    public String getLastPrivateMessage() {
        return this.lastPrivateMessageText;
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
    public void setLastMessageSender(UUID uuid) {
        this.lastPrivateMessageGetterUUID = uuid;
    }

    @Override
    public void setLastPrivateMessage(String message) {
        this.lastPrivateMessageText = message;
    }

    @Override
    public <F extends UserProvidedFeature> F feature(FeatureType<F> type) {
        return null; // TODO Impl
    }

    @Override
    public void saveData() throws IOException {
        if (!this.isOnline())
            manager.getHandler().saveData(this);
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

    @SuppressWarnings({"untested", "Do Not Run If the User is Online"})
    public void clear() {
        if (this.isOnline())
            return;

        manager = null;
        dateFormat = null;
        uuid = null;
        name = null;
        homeHandler = null;
        location = null;
        lastLocation = null;
        lastPrivateMessageGetterUUID = null;
        lastPrivateMessageText = null;
        firstJoin = null;
        settings = null;
    }

    @Override
    public boolean equals(User anotherUser) {
        return anotherUser == this || anotherUser.getUuid().equals(this.uuid) || anotherUser.getUsername().equals(this.getUsername());
    }

    @Nullable
    @Override
    public UserInventory getInventory() {
        return this.inventory;
    }

    @Override
    public boolean ignored(UUID uuid) {
        return this.getSetting(Settings.IGNORE_LIST).containsValue(uuid);
    }

    public static void saveLocationOf(ServerPlayerEntity player) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);

        if (user != null) {
            user.saveLocation();
        }
    }

    public boolean shouldMessage() {
        return true;
    }

    public ServerUser withCachedName() {
        this.name = this.cachedName;
        return this;
    }
}