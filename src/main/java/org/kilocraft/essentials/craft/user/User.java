package org.kilocraft.essentials.craft.user;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

import java.util.Date;
import java.util.UUID;

public class User {
    private UUID uuid;
    private BlockPos lastPos = new BlockPos(0,100 ,0);
    private int lastPosDim = 0;
    private String nickName = "User";
    private boolean isFlyEnabled;
    private boolean isVulnerable;
    private String lastPrivateMessageGetterUUID = "";
    private String lastPrivateMessageText = "";
    private boolean hasJoinedBefore = false;
    private Date firstJoin = new Date();

    public User(UUID uuid) {
        this.uuid = uuid;
    }

    public CompoundTag serialize() {
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
            lastMessageTag.putString("destUUID", this.lastPrivateMessageGetterUUID);
            lastMessageTag.putString("text", this.lastPrivateMessageText);
            cacheTag.put("lastMessage", lastMessageTag);
            cacheTag.putBoolean("isFlyEnabled", this.isFlyEnabled);
            cacheTag.putBoolean("isVulnerable", this.isVulnerable);
        }
        {
            CompoundTag firstJoinTag = new CompoundTag();

            metaTag.put("firstJoin", firstJoinTag);
            metaTag.putBoolean("hasJoinedBefore", this.hasJoinedBefore);
            metaTag.putString("nick", this.nickName);
        }

        mainTag.put("meta", metaTag);
        mainTag.put("cache", cacheTag);
        return mainTag;
    }

    public void deserialize(CompoundTag compoundTag, UUID uuid) {
        User user = new User(uuid);
        {
            user.setNickName(compoundTag.getString("meta.nick"));
            user.setHasJoinedBefore(compoundTag.getBoolean("meta.hasJoinedBefore"));
        }
        {

        }
    }


    public UUID getUuid() {
        return this.uuid;
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

    public String getLastPrivateMessageGetterUUID() {
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

    public void setNickName(String name) {
        this.nickName = name;
    }

    public void setFlyEnabled(boolean set) {
        isFlyEnabled = set;
    }

    public void setIsVulnerable(boolean set) {
        this.isVulnerable = set;
    }

    public void setLastPos(BlockPos pos) {
        this.lastPos = pos;
    }

    public void setLastPosDim(int lastPosDim) {
        this.lastPosDim = lastPosDim;
    }

    public void setLastPrivateMessageGetterUUID(String lastPrivateMessageGetterUUID) {
        this.lastPrivateMessageGetterUUID = lastPrivateMessageGetterUUID;
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

}