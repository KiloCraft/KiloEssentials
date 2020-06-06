package org.kilocraft.essentials.api.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.PlayerManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kilocraft.essentials.api.KiloServer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

public class Litebans2Vanilla {
    JSONParser parser = new JSONParser();
    private static final Pattern UUID_PATTERN = Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");
    public Litebans2Vanilla(File file) {
        Object object = null;
        try {
            object = parser.parse(new FileReader(file));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = (JSONArray) object;
        KiloServer.getLogger().info("Converting " + jsonArray.size() + " users!");
        PlayerManager playerManager = KiloServer.getServer().getMinecraftServer().getPlayerManager();
        int i = 0;
        for (Object o : jsonArray) {
            String ipBan = ((JSONObject) o).get("ipban").toString();
            String id = ((JSONObject) o).get("uuid").toString();
            GameProfile gameProfile = null;
            if(UUID_PATTERN.matcher(id).matches()) {
                UUID uuid = UUID.fromString(((JSONObject) o).get("uuid").toString());
                gameProfile = KiloServer.getServer().getMinecraftServer().getUserCache().getByUuid(uuid);
                gameProfile = (gameProfile == null) ? new GameProfile(uuid, "#imported#") : gameProfile;
            } else {
                gameProfile = new GameProfile(UUID.randomUUID(), "#imported#");
            }
            Date time = new Date(Long.parseLong(((JSONObject) o).get("time").toString()));
            String source = ((JSONObject) o).get("banned_by_name").toString();
            Date until = new Date(Long.parseLong(((JSONObject) o).get("until").toString()));
            until = until.getTime() == -1 ? null : until;
            String reason = ((JSONObject) o).get("reason").toString();
            String ip = ((JSONObject)o).get("ip").toString();
            if(ip.equalsIgnoreCase("#imported#")){
                KiloServer.getLogger().info("Converting " + id);
                BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(gameProfile, time, source, until, reason);
                KiloServer.getServer().getMinecraftServer().getPlayerManager().getUserBanList().add(bannedPlayerEntry);
                i++;
            } else {
                KiloServer.getLogger().info("Converting " + ip);
                BannedIpEntry bannedIpEntry = new BannedIpEntry(ip, time, source, until, reason);
                playerManager.getIpBanList().add(bannedIpEntry);
                i++;
            }
        }
        KiloServer.getLogger().info("Converted " + i + " users!");
    }

}
