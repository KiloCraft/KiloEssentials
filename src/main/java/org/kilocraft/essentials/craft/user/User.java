package org.kilocraft.essentials.craft.user;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author CODY_AI
 * An easy way to handle the User (Instance of player)
 *
 * @see UserManager
 * @see UserHomeHandler
 */

public class User {
    private static UserManager manager = KiloServer.getServer().getUserManager();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    UUID uuid;
    String name = "";
    private UserHomeHandler homeHandler;
    private BlockPos lastPos = new BlockPos(0,-1 ,0);
    private BlockPos pos = new BlockPos(0, -1, 0);
    private int lastPosDim = 0;
    private int posDim = 0;
    private String nickname = "";
    private boolean isFlyEnabled = false;
    private boolean isInvulnerable = false;
    private String lastPrivateMessageGetterUUID = "";
    private String lastPrivateMessageText = "";
    private boolean hasJoinedBefore = true;
    private Date firstJoin = new Date();
    private int randomTeleportsLeft = 3;
    private int displayParticleId = 0;

    public static User of(UUID uuid) {
        return manager.getUser(uuid);
    }

    public static User of(String name) {
        return manager.getUser(name);
    }

    public static User of(GameProfile profile) {
        return of(profile.getId());
    }

    public static User of(ServerPlayerEntity player) {
        return of(player.getUuid());
    }

    public static User getByNickname(String name) {
        return manager.getUserByNickname(name);
    }
    
    public User(UUID uuid) {
        this.uuid = uuid;
        if (UserHomeHandler.isEnabled())
            this.homeHandler = new UserHomeHandler(this);
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
            CompoundTag posTag = new CompoundTag();
            posTag.putDouble("x", this.pos.getX());
            posTag.putDouble("y", this.pos.getY());
            posTag.putDouble("z", this.pos.getZ());
            posTag.putInt("dim", this.posDim);
            mainTag.put("pos", posTag);
        }
        {
            CompoundTag lastMessageTag = new CompoundTag();
            lastMessageTag.putString("destUUID", this.lastPrivateMessageGetterUUID);
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
            this.lastPos = new BlockPos(
                    lastPosTag.getDouble("x"),
                    lastPosTag.getDouble("y"),
                    lastPosTag.getDouble("z")
            );
        }
        {
            CompoundTag posTag = cacheTag.getCompound("pos");
            this.pos = new BlockPos(
                    posTag.getDouble("x"),
                    posTag.getDouble("y"),
                    posTag.getDouble("z")
            );
        }
        {
            CompoundTag lastMessageTag = cacheTag.getCompound("lastMessage");
            this.lastPrivateMessageGetterUUID = lastMessageTag.getString("destUUID");
            this.lastPrivateMessageText = lastMessageTag.getString("text");
        }
        {
            if (cacheTag.getBoolean("isFlyEnabled"))
                this.isFlyEnabled = true;
            if (cacheTag.getBoolean("isInvulnerable"))
                this.isInvulnerable = true;
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
        this.pos = KiloServer.getServer().getPlayer(this.uuid).getBlockPos();
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

    public ServerPlayerEntity getPlayer() {
        return KiloServer.getServer().getPlayer(this.uuid);
    }

    public ServerCommandSource getCommandSource() {
        return this.getPlayer().getCommandSource();
    }

    public UserHomeHandler getHomesHandler() {
        return this.homeHandler;
    }

    public boolean isOnline() {
        return KiloServer.getServer().getPlayerManager().getPlayer(this.uuid) != null;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public String getUuidAsString() {
        return this.uuid.toString();
    }

    public String getNickname() {
        return this.nickname.equals("") ? this.name : this.nickname;
    }

    public BlockPos getLastPos() {
        return this.lastPos;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public boolean isFlyEnabled() {
        return this.isFlyEnabled;
    }

    public int getLastPosDim() {
        return this.lastPosDim;
    }

    public String getLastPrivateMessageGetter() {
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

    public String getFirstJoinAsString() {
        return dateFormat.format(this.firstJoin);
    }
    
    public int getRTPsLeft() {
    	return this.randomTeleportsLeft;
    }
    
    public int getDisplayParticleId () {
    	return this.displayParticleId;
    }

    public String getDisplayNameAsString() {
        return getDisplayName().asString();
    }

    public Text getDisplayName() {
        return manager.getUserDisplayName(this);
    }


    public void setNickname(String name) {
        this.nickname = name;
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

    public void setLastPos(int x, int y, int z) {
        this.lastPos = new BlockPos(x, y, z);
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setPos(int x, int y, int z) {
        this.pos = new BlockPos(x, y, z);
    }

    public void setLastPosDim(int lastPosDim) {
        this.lastPosDim = lastPosDim;
    }

    public void setLastPrivateMessageGetter(String uuid) {
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
    
    public void setRTPsLeft(int amount) {
    	this.randomTeleportsLeft = amount;
    }
    
    public void setDisplayParticleId (int id) {
    	this.displayParticleId = id;
    }

}