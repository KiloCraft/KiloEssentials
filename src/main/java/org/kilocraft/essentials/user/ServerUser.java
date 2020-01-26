package org.kilocraft.essentials.user;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.util.NBTTypes;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private UserHomeHandler homeHandler;
    private Vec3dLocation location;
    private Vec3dLocation lastLocation;
    private String nickname;
    private boolean canFly = false;
    private boolean invulnerable = false;
    private UUID lastPrivateMessageGetterUUID;
    private String lastPrivateMessageText = "";
    private boolean hasJoinedBefore = true;
    private Date firstJoin = new Date();
    private int randomTeleportsLeft = 3;
    private int displayParticleId = 0;
    public int messageCooldown;
    private List<String> subscriptions;
    private String upstreamChannelId;
    private boolean socialSpy = false;
    private boolean commandSpy = false;
    private boolean canSit = false;
    
    public ServerUser(UUID uuid) {
        this.uuid = uuid;
        if (UserHomeHandler.isEnabled()) // TODO Use new feature provider in future
            this.homeHandler = new UserHomeHandler(this);
        try {
            manager.getHandler().handleUser(this);
        } catch (IOException e) {
            KiloEssentials.getLogger().error("Failed to Load User for [" + uuid.toString() + "]");
        }

        this.subscriptions = new ArrayList<>();
    }

    protected CompoundTag serialize() {
        CompoundTag mainTag = new CompoundTag();
        CompoundTag metaTag = new CompoundTag();
        CompoundTag cacheTag = new CompoundTag();

        // Here we store the players current location
        if (this.location == null)
            updateLocation();
        mainTag.put("loc", this.location.toTag());

        // Private messaging stuff
        if(this.getLastPrivateMessageSender() != null) {
            CompoundTag lastMessageTag = new CompoundTag();
            lastMessageTag.putString("destUUID", this.getLastPrivateMessageSender().toString());
            if(this.getLastPrivateMessage() != null) {
                lastMessageTag.putString("text", this.getLastPrivateMessage());
            }
            cacheTag.put("lastMessage", lastMessageTag);
        }

        // Chat channels stuff
        CompoundTag channelsCache = new CompoundTag();

        if (this.upstreamChannelId != null)
            channelsCache.putString("upstreamChannelId", this.upstreamChannelId);
        cacheTag.put("channels", channelsCache);

        // Abilities
        if (this.canFly)
            cacheTag.putBoolean("isFlyEnabled", true);

        if (this.invulnerable)
            cacheTag.putBoolean("isInvulnerable", true);

        if (this.socialSpy)
            cacheTag.putBoolean("socialSpy", true);

        if (this.socialSpy)
            cacheTag.putBoolean("commandSpy", true);

        if (this.canSit)
            cacheTag.putBoolean("canSit", true);

        // TODO When possible, move particle logic to a feature.
        if (this.displayParticleId != 0)
            metaTag.putInt("displayParticleId", this.displayParticleId);

        metaTag.putBoolean("hasJoinedBefore", this.hasJoinedBefore);

        metaTag.putString("firstJoin", dateFormat.format(this.firstJoin));

        if(this.nickname != null) // Nicknames are Optional now.
            metaTag.putString("nick", this.nickname);

        // Home logic, TODO Abstract this with features in future.
        CompoundTag homeTag = new CompoundTag();
        this.homeHandler.serialize(homeTag);
        mainTag.put("homes", homeTag);

        // Misc stuff now.
        mainTag.putInt("rtpLeft", this.randomTeleportsLeft);
        mainTag.put("meta", metaTag);
        mainTag.put("cache", cacheTag);
        mainTag.putString("name", this.name);
        return mainTag;
    }

    protected void deserialize(@NotNull CompoundTag compoundTag) {
    	CompoundTag metaTag = compoundTag.getCompound("meta");
        CompoundTag cacheTag = compoundTag.getCompound("cache");

        if (compoundTag.contains("loc")) {
        	this.location = Vec3dLocation.dummy();
        	this.location.fromTag(compoundTag.getCompound("loc"));
        }

        if(cacheTag.contains("lastMessage", NBTTypes.COMPOUND)) {
            CompoundTag lastMessageTag = cacheTag.getCompound("lastMessage");
            if(lastMessageTag.contains("destUUID", NBTTypes.STRING))
                this.lastPrivateMessageGetterUUID = UUID.fromString(lastMessageTag.getString("destUUID"));

            if(lastMessageTag.contains("text", NBTTypes.STRING))
                this.lastPrivateMessageText = lastMessageTag.getString("text");

        }

        if (cacheTag.contains("channels", NBTTypes.COMPOUND)) {
            CompoundTag channelsTag = cacheTag.getCompound("channels");

            if (channelsTag.contains("upstreamChannelId", NBTTypes.STRING))
                this.upstreamChannelId = channelsTag.getString("upstreamChannelId");
            else
                this.upstreamChannelId = GlobalChat.getChannelId();
        }

        if (cacheTag.getBoolean("isFlyEnabled")) {
            this.canFly = true;
        }
        
        if (cacheTag.getBoolean("isInvulnerable")) {
            this.invulnerable = true;
        }


        if (cacheTag.contains("socialSpy"))
            this.socialSpy = cacheTag.getBoolean("socialSpy");
        if (cacheTag.contains("commandSpy"))
            this.commandSpy = cacheTag.getBoolean("commandSpy");

        if (cacheTag.contains("canSit"))
            this.canSit = cacheTag.getBoolean("canSit");

        if (metaTag.getInt("displayParticleId") != 0)
            this.displayParticleId = metaTag.getInt("displayParticleId");

        this.hasJoinedBefore = metaTag.getBoolean("hasJoinedBefore");
        this.firstJoin = getUserFirstJoinDate(metaTag.getString("firstJoin"));

        if (metaTag.contains("nick")) // Nicknames are an Optional, so we compensate for that.
            this.nickname = metaTag.getString("nick");

        this.homeHandler.deserialize(compoundTag.getCompound("homes"));
        this.randomTeleportsLeft = compoundTag.getInt("rtpLeft");
    }

    public void updateLocation() {
        if (this instanceof OnlineUser) this.location = Vec3dLocation.of((OnlineUser) this);
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

    @Override
    public boolean canSit() {
        return this.canSit;
    }

    @Override
    public void setCanSit(boolean set) {
        this.canSit = set;
    }

    @Override
    public boolean isOnline() {
        return KiloServer.getServer().getPlayerManager().getPlayer(this.uuid) != null;
    }

    @Override
    public boolean hasNickname() {
        return this.getNickname().isPresent();
    }

    @Override
    public String getDisplayname() {
        return (hasNickname()) ? this.nickname : this.name;
    }

    @Override
    public String getFormattedDisplayname() {
        return TextFormat.translate(getDisplayname() + "&r");
    }

    @Override
    public Text getRankedDisplayname() {
        return Team.modifyText(
                KiloServer.getServer().getPlayer(this.uuid).getScoreboardTeam(), new LiteralText(getDisplayname()));
    }

    @Override
    public List<String> getSubscriptionChannels() {
        return this.subscriptions;
    }

    @Override
    public String getUpstreamChannelId() {
        return (this.upstreamChannelId != null) ? this.upstreamChannelId : GlobalChat.getChannelId();
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
    public Optional<String> getNickname() {
        return Optional.ofNullable(this.nickname);
    }

    @Override
    public Location getLocation() {
        if (this.location == null)
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
            this.lastLocation = Vec3dLocation.of((OnlineUser) this);
    }

    @Override
    public void setNickname(String name) {
        String oldNick = this.nickname;
        this.nickname = name;
        KiloServer.getServer().getUserManager().onChangeNickname(this, oldNick); // This is to update the entries in UserManager.
    }

    @Override
    public void clearNickname() {
        this.nickname = null;
    }

    @Override
    public void setLastLocation(Location loc) {
        this.lastLocation = (Vec3dLocation) loc;
    }

    @Override
    public boolean canFly() {
        return this.canFly;
    }

    @Override
    public boolean isSocialSpyOn() {
        return this.socialSpy;
    }

    @Override
    public void setSocialSpyOn(boolean on) {
        this.socialSpy = on;
    }

    @Override
    public boolean isCommandSpyOn() {
        return this.commandSpy;
    }

    @Override
    public void setCommandSpyOn(boolean on) {
        this.commandSpy = on;
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
    public void setUpstreamChannelId(String id) {
        this.upstreamChannelId = id;
    }

    @Override
    public boolean isInvulnerable() {
        return this.invulnerable;
    }

    public int getRTPsLeft() {
    	return this.randomTeleportsLeft;
    }

    public void setRTPsLeft(int amount) {
        this.randomTeleportsLeft = amount;
    }
    
    public int getDisplayParticleId () {
    	return this.displayParticleId;
    }

    public void setFlight(boolean set) {
        canFly = set;
    }

    public void setInvulnerable(boolean set) {
        this.invulnerable = set;
    }

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

    public void setDisplayParticleId (int id) {
    	this.displayParticleId = id;
    }

    public void resetMessageCooldown() {
        if (this.messageCooldown > 0) {
            --this.messageCooldown;
        }
    }

}