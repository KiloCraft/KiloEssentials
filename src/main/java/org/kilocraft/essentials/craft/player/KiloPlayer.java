package org.kilocraft.essentials.craft.player;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;

public class KiloPlayer {
	public UUID playerId;
	public String nick = "";
	public int rtpLeft = 3;
	public String particle = "none";

	public KiloPlayer(UUID id) {
		playerId = id;
	}
	
	public KiloPlayer(String id, CompoundTag tag) {
		playerId = UUID.fromString(id);
		fromTag(tag);
	}

	public CompoundTag toTag() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("nick", nick);
		compoundTag.putInt("rtpLeft", rtpLeft);
		compoundTag.putString("particle", particle);
		return compoundTag;
	}

	public void fromTag(CompoundTag tag) {
		this.nick = tag.getString("nick");
		this.rtpLeft = tag.getInt("rtpLeft");
		this.particle = tag.getString("particle");
	}
}
