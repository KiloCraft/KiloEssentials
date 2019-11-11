package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.UserManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author CODY_AI
 * An easy way to handle the User (Instance of player)
 *
 * @see ServerUserManager_Old
 * @see UserHomeHandler
 */

public class ServerUser implements User {
    protected static ServerUserManager manager = (ServerUserManager) KiloServer.getServer().getUserManager();
    private static ServerUserManager_Old managerOLD = KiloServer.getServer().getUserManagerOLD();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    UUID uuid;
    String name = "";
    private UserHomeHandler homeHandler;
    private Vec3d backPos = Vec3d.ZERO;
    private Vec3d pos = Vec3d.ZERO;
    private Identifier lastPosDim;
    private Identifier posDim;
    private String nickname = "";
    private boolean canFly = false;
    private boolean invulnerable = false;
    private UUID lastPrivateMessageGetterUUID;
    private String lastPrivateMessageText = "";
    private boolean hasJoinedBefore = true;
    private Date firstJoin = new Date();
    private int randomTeleportsLeft = 3;
    private int displayParticleId = 0;
    
    public ServerUser(UUID uuid) {
        this.uuid = uuid;
        if (UserHomeHandler.isEnabled()) // TODO Use new feature provider in future
            this.homeHandler = new UserHomeHandler(this);
    }


    CompoundTag serialize() {
        CompoundTag mainTag = new CompoundTag();
        CompoundTag metaTag = new CompoundTag();
        CompoundTag cacheTag = new CompoundTag();
        {
            CompoundTag lastPosTag = new CompoundTag();
            lastPosTag.putDouble("x", this.backPos.getX());
            lastPosTag.putDouble("y", this.backPos.getY());
            lastPosTag.putDouble("z", this.backPos.getZ());
            lastPosTag.putString("dim", this.lastPosDim.toString());
            cacheTag.put("lastPos", lastPosTag);
        }
        {
            CompoundTag posTag = new CompoundTag();
            posTag.putDouble("x", this.pos.getX());
            posTag.putDouble("y", this.pos.getY());
            posTag.putDouble("z", this.pos.getZ());
            posTag.putString("dim", this.posDim.toString());
            mainTag.put("pos", posTag);
        }
        {
            CompoundTag lastMessageTag = new CompoundTag();
            lastMessageTag.putString("destUUID", this.lastPrivateMessageGetterUUID.toString());
            lastMessageTag.putString("text", this.lastPrivateMessageText);
            cacheTag.put("lastMessage", lastMessageTag);

            if (this.canFly)
                cacheTag.putBoolean("isFlyEnabled", true);
            if (this.invulnerable)
                cacheTag.putBoolean("isInvulnerable", true);
        }
        {
            if (this.displayParticleId != 0)
                metaTag.putInt("displayParticleId", this.displayParticleId);

            metaTag.putBoolean("hasJoinedBefore", this.hasJoinedBefore);

            metaTag.putString("firstJoin", dateFormat.format(this.firstJoin));
            metaTag.putString("nick", this.nickname);
        }
        {
            CompoundTag homeTag = new CompoundTag();
            this.homeHandler.serialize(homeTag);
            mainTag.put("homes", homeTag);
        }

        //mainTag.put("homes", this.homeHandler.serialize());
        mainTag.putInt("rtpLeft", this.randomTeleportsLeft);
        mainTag.put("meta", metaTag);
        mainTag.put("cache", cacheTag);
        mainTag.putString("name", this.name);
        return mainTag;
    }

    void deserialize(@NotNull CompoundTag compoundTag) {
        CompoundTag metaTag = compoundTag.getCompound("meta");
        CompoundTag cacheTag = compoundTag.getCompound("cache");

        {
            CompoundTag lastPosTag = cacheTag.getCompound("lastPos");
            this.backPos = new Vec3d(
                    lastPosTag.getDouble("x"),
                    lastPosTag.getDouble("y"),
                    lastPosTag.getDouble("z")
            );
        }
        {
            CompoundTag posTag = cacheTag.getCompound("pos");
            this.pos = new Vec3d(
                    posTag.getDouble("x"),
                    posTag.getDouble("y"),
                    posTag.getDouble("z")
            );
        }
        {
            CompoundTag lastMessageTag = cacheTag.getCompound("lastMessage");
            this.lastPrivateMessageGetterUUID = UUID.fromString(lastMessageTag.getString("destUUID"));
            this.lastPrivateMessageText = lastMessageTag.getString("text");
        }
        {
            if (cacheTag.getBoolean("isFlyEnabled"))
                this.canFly = true;
            if (cacheTag.getBoolean("isInvulnerable"))
                this.invulnerable = true;
        }
        {
            if (compoundTag.getInt("displayParticleId") != 0)
                this.displayParticleId = compoundTag.getInt("displayParticleId");

            this.hasJoinedBefore = metaTag.getBoolean("hasJoinedBefore");
            this.firstJoin = getUserFirstJoinDate(metaTag.getString("firstJoin"));
            this.nickname = metaTag.getString("nick");
        }

        this.homeHandler.deserialize(compoundTag.getCompound("homes"));
        this.randomTeleportsLeft = compoundTag.getInt("rtpLeft");
    }

    public void updatePos() {
        this.pos = KiloServer.getServer().getPlayer(this.uuid).getPos();
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
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public String getNickname() {
        return this.nickname.equals("") ? this.name : this.nickname;
    }

    @Override
    public void setNickname(String name) {
        this.nickname = name;
    }

    @Override
    public void clearNickname() {
        this.nickname = "";
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
    public <F extends UserProvidedFeature> F feature(FeatureType<F> type) {
        return null; // TODO Impl
    }

    public void setDisplayParticleId (int id) {
    	this.displayParticleId = id;
    }
}