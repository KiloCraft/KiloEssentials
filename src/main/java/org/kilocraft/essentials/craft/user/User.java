package org.kilocraft.essentials.craft.user;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.homesystem.Home;
import org.kilocraft.essentials.craft.homesystem.HomeManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class User {
    private static UserManager manager = KiloServer.getServer().getUserManager();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    UUID uuid;
    String name = "";
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
    private List<Home> homes = new ArrayList<>();

    public static User of(UUID uuid) {
        return new User(uuid);
    }

    public static User of(String name) {
        return manager.getUser(name);
    }

    public static User of(ServerPlayerEntity player) {
        return of(player.getUuid());
    }

    public static User getByNickname(String name) {
        return manager.getUserByNickname(name);
    }
    
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
            if (!hasJoinedBefore)
                metaTag.putString("firstJoin", dateFormat.format(new Date()));
            else
                metaTag.putString("firstJoin", dateFormat.format(this.firstJoin));
            metaTag.putString("nick", this.nickname);
        }

        mainTag.putInt("randomTeleportsLeft", this.randomTeleportsLeft);
        mainTag.put("meta", metaTag);
        mainTag.put("cache", cacheTag);
        mainTag.putString("name", this.name);
        return mainTag;
    }

    void deserialize(CompoundTag compoundTag, UUID uuid) {
        User user = new User(uuid);
        {
            user.setHasJoinedBefore(compoundTag.getBoolean("meta.hasJoinedBefore"));
            user.setFirstJoin(getUserFirstJoinDate(compoundTag));
        }
        {
            user.setLastPrivateMessageGetter(compoundTag.getString("lastMessage.destUUID"));
            user.setLastPrivateMessageText(compoundTag.getString("lastMessage.text"));
        }
        {
            user.lastPos = new BlockPos(
                    compoundTag.getDouble("cache.lastPos.x"),
                    compoundTag.getDouble("cache.lastPos.y"),
                    compoundTag.getDouble("cache.lastPos.z")
            );
        }
        {
            if (compoundTag.getBoolean("cache.isFlyEnabled"))
                user.setFlyEnabled(true);
            if (compoundTag.getBoolean("cache.isInvulnerable"))
                user.setIsInvulnerable(true);
        }
        {
            user.setNickname(compoundTag.getString("meta.nick"));
            user.setDisplayParticleId(compoundTag.getInt("meta.displayParticleId"));
        }
        
        user.setRandomTeleportsLeft(compoundTag.getInt("randomTeleportsLeft"));
        user.setDisplayParticleId(compoundTag.getInt("particle"));
        user.name = compoundTag.getString("name");
    }

    public void updatePos() {
        this.pos = KiloServer.getServer().getPlayer(this.uuid).getBlockPos();
    }

    private Date getUserFirstJoinDate(CompoundTag compoundTag) {
        Date date = new Date();
        try {
            date = dateFormat.parse(compoundTag.getString("meta.firstJoin"));
        } catch (ParseException e) {
            /*
             * Pass, this is the first time that user is joined.
             */
        }
        return date;
    }

    public ServerPlayerEntity getPlayer() {
        return KiloServer.getServer().getPlayer(this.uuid);
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
    
    public int getRandomTeleportsLeft() {
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

    public List<Home> getHomes() {
        return this.homes;
    }

    public Home getHome(String name) {
        return HomeManager.getHome(this.getUuid(), name);
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
    
    public void setRandomTeleportsLeft(int randomTeleportsLeft) {
    	this.randomTeleportsLeft = randomTeleportsLeft;
    }
    
    public void setDisplayParticleId (int id) {
    	this.displayParticleId = id;
    }

}