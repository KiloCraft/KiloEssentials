package org.kilocraft.essentials.craft.player;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;

public class KiloPlayer {
	public UUID playerId;
	public String nick = "";

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
		return compoundTag;
	}

	public void fromTag(CompoundTag tag) {
		this.nick = tag.getString("nick");
	}
}
