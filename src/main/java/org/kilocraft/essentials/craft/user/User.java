package org.kilocraft.essentials.craft.user;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class User {
    private UUID uuid;
    private BlockPos lastPos;
    private String nickName;

    public User(ServerPlayerEntity serverPlayer) {
        this.uuid = serverPlayer.getUuid();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String name) {
        this.nickName = name;
    }

    public BlockPos getLastPos() {
        return this.lastPos;
    }

    public void setLastPos(BlockPos pos) {
        this.lastPos = pos;
    }

    public void toTag(CompoundTag compoundTag) {

    }

    public CompoundTag fromTag() {
        CompoundTag compoundTag = new CompoundTag();

        return compoundTag;
    }
}
