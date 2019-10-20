package org.kilocraft.essentials.craft.player;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class KiloPlayer {
	public UUID playerId;
	public String nick = "";
	public int rtpLeft = 3;
	public String particle = "";

	public KiloPlayer(UUID id) {
		playerId = id;
	}
	
	public KiloPlayer(String id, CompoundTag tag) {
		this.playerId = UUID.fromString(id);
		fromTag(tag);
	}

	public CompoundTag toTag() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("nick", this.nick);
		compoundTag.putInt("rtpLeft", this.rtpLeft);
		compoundTag.putString("particle", this.particle);
		return compoundTag;
	}

	public void fromTag(CompoundTag tag) {
		this.nick = tag.getString("nick");
		this.rtpLeft = tag.getInt("rtpLeft");
		this.particle = tag.getString("particle");
	}
}
