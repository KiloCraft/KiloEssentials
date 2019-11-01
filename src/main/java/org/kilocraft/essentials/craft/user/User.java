package org.kilocraft.essentials.craft.user;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class User {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private UUID uuid;
    private BlockPos lastPos = new BlockPos(0,-1 ,0);
    private int lastPosDim = 0;
    private String nickName = "";
    private boolean isFlyEnabled = false;
    private boolean isInvulnerable = false;
    private UUID lastPrivateMessageGetterUUID;
    private String lastPrivateMessageText = "";
    private boolean hasJoinedBefore = false;
    private Date firstJoin = new Date();
    private int randomTeleportsLeft = 3;
    private int displayParticleId = 0;

    public User(UUID uuid) {
        this.uuid = uuid;
    }

    CompoundTag serialize() {
        CompoundTag mainTag = new CompoundTag();
        CompoundTag metaTag = new CompoundTag();
        CompoundTag cacheTag = new CompoundTag();
        {
            CompoundTag lastPosTag = new CompoundTag();
            lastPosTag.putDouble("x", this.lastPos.getX());
            lastPosTag.putDouble("y", this.lastPos.getY());
            lastPosTag.putDouble("z", this.lastPos.getZ());
            lastPosTag.putInt("dim", this.lastPosDim);
            cacheTag.put("lastPos", lastPosTag);
        }
        {
            CompoundTag lastMessageTag = new CompoundTag();
            lastMessageTag.putUuid("destUUID", this.lastPrivateMessageGetterUUID);
            lastMessageTag.putString("text", this.lastPrivateMessageText);
            cacheTag.put("lastMessage", lastMessageTag);

            if (this.isFlyEnabled)
                cacheTag.putBoolean("isFlyEnabled", true);
            if (this.isInvulnerable)
                cacheTag.putBoolean("isInvulnerable", true);
        }
        {

            if (this.displayParticleId != 0)
                metaTag.putInt("displayParticleId", this.displayParticleId);

            metaTag.putBoolean("hasJoinedBefore", this.hasJoinedBefore);
            metaTag.putString("firstJoin", dateFormat.format(firstJoin));
            metaTag.putString("nick", this.nickName);
        }

        mainTag.putInt("randomTeleportsLeft", this.randomTeleportsLeft);
        mainTag.put("meta", metaTag);
        mainTag.put("cache", cacheTag);
        return mainTag;
    }

    void deserialize(CompoundTag compoundTag, UUID uuid) {
        User user = new User(uuid);
        {
            user.setHasJoinedBefore(compoundTag.getBoolean("meta.hasJoinedBefore"));
            user.setFirstJoin(getUserFirstJoinDate(compoundTag));
        }
        {
            user.setLastPrivateMessageGetter(compoundTag.getUuid("lastMessage.destUUID"));
            user.setLastPrivateMessageText(compoundTag.getString("lastMessage.text"));
        }
        {
            if (compoundTag.getBoolean("cache.isFlyEnabled"))
                user.setFlyEnabled(true);
            if (compoundTag.getBoolean("cache.isInvulnerable"))
                user.setIsInvulnerable(true);
        }
        {
            user.setNickName(compoundTag.getString("meta.nick"));
            user.setDisplayParticleId(compoundTag.getInt("meta.displayParticleId"));
        }
        
        user.setRandomTeleportsLeft(compoundTag.getInt("randomTeleportsLeft"));
        user.setDisplayParticleId(compoundTag.getInt("particle"));
    }

    private Date getUserFirstJoinDate(CompoundTag compoundTag) {
        Date date = new Date();
        try {
            date = dateFormat.parse(compoundTag.getString("meta.firstJoin"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public UUID getUuid() {
        return this.uuid;
    }

    public String getUuidAsString() {
        return this.uuid.toString();
    }

    public String getNickName() {
        return this.nickName;
    }

    public BlockPos getLastPos() {
        return this.lastPos;
    }

    public boolean isFlyEnabled() {
        return this.isFlyEnabled;
    }

    public int getLastPosDim() {
        return this.lastPosDim;
    }

    public UUID getLastPrivateMessageGetter() {
        return this.lastPrivateMessageGetterUUID;
    }

    public String getLastPrivateMessageText() {
        return this.lastPrivateMessageText;
    }

    public boolean isHasJoinedBefore() {
        return this.hasJoinedBefore;
    }

    public Date getFirstJoin() {
        return this.firstJoin;
    }
    
    public int getRandomTeleportsLeft() {
    	return this.randomTeleportsLeft;
    }
    
    public int getDisplayParticleId () {
    	return this.displayParticleId;
    }


    public void setNickName(String name) {
        this.nickName = name;
    }

    public void setFlyEnabled(boolean set) {
        isFlyEnabled = set;
    }

    public void setIsInvulnerable(boolean set) {
        this.isInvulnerable= set;
    }

    public void setLastPos(BlockPos pos) {
        this.lastPos = pos;
    }

    public void setLastPosDim(int lastPosDim) {
        this.lastPosDim = lastPosDim;
    }

    public void setLastPrivateMessageGetter(UUID uuid) {
        this.lastPrivateMessageGetterUUID = uuid;
    }

    public void setLastPrivateMessageText(String lastPrivateMessageText) {
        this.lastPrivateMessageText = lastPrivateMessageText;
    }

    public void setHasJoinedBefore(boolean hasJoinedBefore) {
        this.hasJoinedBefore = hasJoinedBefore;
    }

    public void setFirstJoin(Date firstJoin) {
        this.firstJoin = firstJoin;
    }
    
    public void setRandomTeleportsLeft(int randomTeleportsLeft) {
    	this.randomTeleportsLeft = randomTeleportsLeft;
    }
    
    public void setDisplayParticleId (int id) {
    	this.displayParticleId = id;
    }

}