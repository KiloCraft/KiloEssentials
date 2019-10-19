package org.kilocraft.essentials.craft.player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.kilocraft.essentials.craft.config.KiloConifg;
import io.github.indicode.fabric.worlddata.NBTWorldData;
import io.github.indicode.fabric.worlddata.WorldDataLib;
import net.minecraft.nbt.CompoundTag;

public class KiloPlayerManager extends NBTWorldData {
	public static KiloPlayerManager INSTANCE = new KiloPlayerManager();
	private static ArrayList<String> byUUID = new ArrayList<>();
	private static List<KiloPlayer> players = new ArrayList<>();

	public static List<KiloPlayer> getPlayersData() {
		return players;
	}

	public static ArrayList<String> getPlayersDataByName() {
		return byUUID;
	}

	public static void addPlayer(KiloPlayer player) {
		players.add(player);
		byUUID.add(player.playerId.toString());
	}

	public static void removePlayer(KiloPlayer player) {
		players.remove(player);
		byUUID.remove(player.playerId.toString());
	}

	public static KiloPlayer getPlayerData(UUID playerId) {
		for (KiloPlayer player : players) {
			if (playerId.equals(player.playerId)) {
				return player;
			}
		}

		KiloPlayer player = new KiloPlayer(playerId);
		addPlayer(player);
		return player;
	}

	public void reload() {
		WorldDataLib.triggerCallbackLoad(this);
	}

	public void save() {
		WorldDataLib.triggerCallbackSave(this);
	}

	public static String[] getPlayersDataAsArray() {
		return players.stream().toArray(String[]::new);
	}

	@Override
	public CompoundTag toNBT(CompoundTag compoundTag) {
		players.forEach(player -> compoundTag.put(player.playerId.toString(), player.toTag()));

		return compoundTag;
	}

	@Override
	public void fromNBT(CompoundTag compoundTag) {
		players.clear();
		byUUID.clear();
		compoundTag.getKeys().forEach((key) -> {
			players.add(new KiloPlayer(key, compoundTag.getCompound(key)));
			byUUID.add(key);
		});
	}

	@Override
	public File getSaveFile(File worldDirectory, File rootDirectory, boolean backup) {
		return new File(KiloConifg.getWorkingDirectory() + "/extraplayerdata." + (backup ? "dat_old" : "dat"));
	}
}
