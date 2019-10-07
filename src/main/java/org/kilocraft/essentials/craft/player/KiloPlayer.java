package org.kilocraft.essentials.craft.player;

import com.electronwill.nightconfig.core.file.FileConfig;
import io.github.indicode.fabric.tinyconfig.DefaultedJsonObject;
import io.github.indicode.fabric.tinyconfig.ModConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.data.KiloData;
import org.kilocraft.essentials.craft.homesystem.Home;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class KiloPlayer {
    private FileConfig playersData = KiloData.getPlayersData();
    private String dataFilePath = "players.";
    private ServerPlayerEntity serverPlayerEntity;

    private String nickname;
    private String name;
    private UUID uuid;
    private List<Home> homes;
    private List<String> properties;
    private BlockPos lastPos;

    static String path = "players.";
    static ModConfig data = new ModConfig("KiloEssentials-Player");

    public KiloPlayer(ServerPlayerEntity player) {
        if (uuid != player.getUuid()) assign(player);
    }

    private void assign(ServerPlayerEntity player) {
        this.serverPlayerEntity = player;
        this.uuid = player.getUuid();
        this.name = player.getGameProfile().getName();

        if (!loadData(this)) saveData(this);
    }

    public static KiloPlayer get(ServerPlayerEntity playerEntity) {
        return new KiloPlayer(playerEntity);
    }

    public static KiloPlayer get(String name) {
        return new KiloPlayer(Objects.requireNonNull(KiloServer.getServer().getPlayerManager().getPlayer(name)));
    }

    public static KiloPlayer get(UUID uuid) {
        return new KiloPlayer(Objects.requireNonNull(KiloServer.getServer().getPlayerManager().getPlayer(uuid)));
    }

    private static String getDataPath(UUID uuid, String path) {
        return path + uuid + "." + path;
    }

    protected static void saveData(KiloPlayer kiloPlayer) {
        String s = path + kiloPlayer.uuid + ".";
        data.configure((config) -> {
            config.setString(getDataPath(kiloPlayer.uuid, "nick"), kiloPlayer.nickname);
            config.setInt(getDataPath(kiloPlayer.uuid, "lastPos.x"), kiloPlayer.lastPos.getX());
            config.setInt(getDataPath(kiloPlayer.uuid, "lastPos.y"), kiloPlayer.lastPos.getY());
            config.setInt(getDataPath(kiloPlayer.uuid, "lastPos.z"), kiloPlayer.lastPos.getZ());
        });
        data.saveConfig(new DefaultedJsonObject());
    }

    protected static boolean loadData(KiloPlayer kiloPlayer) {
        data.configure((config) -> {
            kiloPlayer.nickname = config.getString(getDataPath(kiloPlayer.uuid, "nick"), "");
            kiloPlayer.lastPos = new BlockPos(
                    config.getInt(getDataPath(kiloPlayer.uuid, "lastPos.x"), 0),
                    config.getInt(getDataPath(kiloPlayer.uuid, "lastPos.y"), 0),
                    config.getInt(getDataPath(kiloPlayer.uuid, "lastPos.z"), 0)
            );
        });

        //Returns true if the player data is successfully loaded
        return kiloPlayer.uuid != null;
    }

    private void unload() {
        this.uuid = null;
        this.playersData = null;
        this.nickname = null;
        this.homes = null;
        this.lastPos = null;
        this.properties = null;
    }

    public ServerPlayerEntity getServerPlayerEntity() {
        return serverPlayerEntity;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<Home> getHomes() {
        return homes;
    }

    public List<String> getProperties() {
        return properties;
    }

    public BlockPos getLastPos() {
        return lastPos;
    }

}
