package org.kilocraft.essentials.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.bungee.KiloEssentialsBungee;
import org.kilocraft.essentials.bungee.KiloEssentialsBungeePlugin;
import org.kilocraft.essentials.bungee.api.ProxiedPlayerUtil;
import org.kilocraft.essentials.config.KiloConfig;

import java.io.*;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class GlobalUtils {
    private static boolean isProxy = KiloConfig.main().server().proxyMode && KiloEssentialsBungeePlugin.isEnabled();

    @Nullable
    public static SocketAddress getSocketAddress(UUID uuid) {
        if (isProxy) {
            return ProxiedPlayerUtil.getRealSocketAddress(uuid);
        }

        OnlineUser user = KiloServer.getServer().getOnlineUser(uuid);
        return user != null && user.getConnection() != null ? user.getConnection().getAddress() : null;
    }

    public static JsonObject jsonFromUrl(String url) throws IOException {
        JsonObject jsonObject;
        InputStream input = new URL(url).openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String json = readAll(reader);
        jsonObject = new JsonParser().parse(json).getAsJsonObject();

        return jsonObject;
    }

    private static String readAll(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        int cp;
        while ((cp = reader.read()) != -1) {
            builder.append((char) cp);
        }
        return builder.toString();
    }

}
