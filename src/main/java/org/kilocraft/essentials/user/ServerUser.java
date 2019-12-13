package org.kilocraft.essentials.user;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.user.User;
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
    private Vec3d backPos = Vec3d.ZERO;
    private Vec3d pos = Vec3d.ZERO;
    private Identifier lastPosDim;
    private Identifier posDim;
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

        // Here we store the players previous position that is refered to with /back.
        if(this.getBackPos() != null && this.getBackDimId() != null) { // Well this crashes without a safety check.
            CompoundTag lastPosTag = new CompoundTag();
            lastPosTag.putDouble("x", this.backPos.getX());
            lastPosTag.putDouble("y", this.backPos.getY());
            lastPosTag.putDouble("z", this.backPos.getZ());
            lastPosTag.putString("dim", this.lastPosDim.toString());
            cacheTag.put("lastPos", lastPosTag);
        }

        // Now the User's real position.
        CompoundTag posTag = new CompoundTag();
        posTag.putDouble("x", this.pos.getX());
        posTag.putDouble("y", this.pos.getY());
        posTag.putDouble("z", this.pos.getZ());

        if(this.posDim == null) { // This should be impossible
            // TODO Notify admins and throw a giant error log into Console to reflect error. Set it to a temp value
            this.posDim = new Identifier("minecraft", "overworld");
        }

        posTag.putString("dim", this.posDim.toString());
        mainTag.put("pos", posTag);

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
        CompoundTag subscriptionsTag = new CompoundTag();

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

        if (cacheTag.contains("lastPos")) {
        	CompoundTag lastPosTag = cacheTag.getCompound("lastPos");
        	this.backPos = new Vec3d(
        		lastPosTag.getDouble("x"),
                lastPosTag.getDouble("y"),
                lastPosTag.getDouble("z")
        	);
        	this.lastPosDim = new Identifier(lastPosTag.getString("dim"));
        }

        CompoundTag posTag = cacheTag.getCompound("pos");
        this.pos = new Vec3d(
                posTag.getDouble("x"),
                posTag.getDouble("y"),
                posTag.getDouble("z")
        );
        this.posDim = new Identifier(posTag.getString("dim"));

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


        this.socialSpy = cacheTag.getBoolean("socialSpy");
        this.commandSpy = cacheTag.getBoolean("socialSpy");

        if (metaTag.getInt("displayParticleId") != 0)
            this.displayParticleId = metaTag.getInt("displayParticleId");

        this.hasJoinedBefore = metaTag.getBoolean("hasJoinedBefore");
        this.firstJoin = getUserFirstJoinDate(metaTag.getString("firstJoin"));

        if (metaTag.contains("nick")) // Nicknames are an Optional, so we compensate for that.
            this.nickname = metaTag.getString("nick");

        this.homeHandler.deserialize(compoundTag.getCompound("homes"));
        this.randomTeleportsLeft = compoundTag.getInt("rtpLeft");
    }

    public void updatePos() {
        this.pos = KiloServer.getServer().getPlayer(this.uuid).getPos();
        this.posDim = Registry.DIMENSION.getId(KiloServer.getServer().getPlayer(this.uuid).getServerWorld().getDimension().getType());
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
    public Vec3d getBackPos() {
        return this.backPos;
    }

    @Override
    public Vec3d getPos() {
        return this.pos;
    }

    @Override
    public Identifier getPosDim() {
        return this.posDim;
    }

    @Override
    public Identifier getBackDimId() {
        return this.lastPosDim;
    }

    @Override
    public void setBackPos(Vec3d pos) {
        this.backPos = pos;
    }

    @Override
    public void setBackDim(Identifier dimId) {
        this.lastPosDim = dimId;
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
    public void addSubscriptionChannel(String id) {
        this.subscriptions.add(id);
    }

    @Override
    public void removeSubscriptionChannel(String id) {
        this.subscriptions.remove(id);
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