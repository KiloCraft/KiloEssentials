package org.kilocraft.essentials.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.kilocraft.essentials.api.user.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * @author mine-care (AKA fillpant)
 * This class performs a name lookup for a player and gets back all the name changes of the player (if any).
 * @since 25-3-2016
 */
public class NameLookup {

    /**
     * The URL from Mojang API that provides the JSON String in response.
     */
    private static final String LOOKUP_URL = "https://api.mojang.com/user/profiles/%s/names";

    /**
     * The URL from Mojang API to resolve the UUID of a player from their name.
     */
    private static final String GET_UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s?t=0";

    /**
     * The URL from Mojang API to resolve the current name of a player from their UUID
     */
    private static final String GET_NAME_URL = "https://api.mojang.com/users/profiles/minecraft/%s?t=0";

    private static final Gson JSON_PARSER = new Gson();

    /**
     * <h1>NOTE: Avoid running this method <i>Synchronously</i> with the main thread!It blocks while attempting to get a response from Mojang servers!</h1>
     * @param uuid The UUID of the player to be looked up.
     * @return Returns an array of {@link PreviousPlayerNameEntry} objects, or null if the response couldn't be interpreted.
     * @throws IOException {@link #getPlayerPreviousNames(String)}
     */
    public static PreviousPlayerNameEntry[] getPlayerPreviousNames(UUID uuid) throws IOException {
        return getPlayerPreviousNames(uuid.toString());
    }

    /**
     * <h1>NOTE: Avoid running this method <i>Synchronously</i> with the main thread! It blocks while attempting to get a response from Mojang servers!</h1>
     * Alternative method accepting an 'OfflinePlayer' (and therefore 'Player') objects as parameter.
     * @param user The OfflinePlayer object to obtain the UUID from.
     * @return Returns an array of {@link PreviousPlayerNameEntry} objects, or null if the response couldn't be interpreted.
     * @throws IOException {@link #getPlayerPreviousNames(UUID)}
     */
    public static PreviousPlayerNameEntry[] getPlayerPreviousNames(User user) throws IOException {
        return getPlayerPreviousNames(user.getUsername());
    }

    /**
     * <h1>NOTE: Avoid running this method <i>Synchronously</i> with the main thread! It blocks while attempting to get a response from Mojang servers!</h1>
     * Alternative method accepting an {@link User} (and therefore {@link net.minecraft.server.network.ServerPlayerEntity}) objects as parameter.
     * @param uuid The UUID String to lookup
     * @return Returns an array of {@link PreviousPlayerNameEntry} objects, or null if the response couldn't be interpreted.
     * @throws IOException {@link #getRawJsonResponse(URL)} )}
     */
    public static PreviousPlayerNameEntry[] getPlayerPreviousNames(String uuid) throws IOException {
        if (uuid == null || uuid.isEmpty())
            return null;
        String response = getRawJsonResponse(new URL(String.format(LOOKUP_URL, uuid)));
        PreviousPlayerNameEntry[] names = JSON_PARSER.fromJson(response, PreviousPlayerNameEntry[].class);
        return names;
    }

    /**
     * If you don't have the UUID of a player, this method will resolve it for you.<br>
     * The output of this method may be used directly with {@link #getPlayerPreviousNames(String)}.<br>
     * <b>NOTE: as with the rest, this method opens a connection with a remote server, so running it synchronously will block the main thread which will lead to server lag.</b>
     * @param name The name of the player to lookup.
     * @return A String which represents the player's UUID. <b>Note: the uuid cannot be parsed to a UUID object directly, as it doesnt contain dashes. This feature will be implemented later</b>
     * @throws IOException Inherited by {@link BufferedReader#readLine()}, {@link BufferedReader#close()}, {@link URL}, {@link HttpURLConnection#getInputStream()}
     */
    public static String getPlayerUUID(String name) throws IOException {
        String response = getRawJsonResponse(new URL(String.format(GET_UUID_URL, name)));
        JsonObject o = JSON_PARSER.fromJson(response, JsonObject.class);
        if (o == null)
            return null;
        return o.get("id") == null ? null : o.get("id").getAsString();
    }

    /**
     * If you don't have the Username of a player, this method will resolve it for you.<br>
     * <b>NOTE: as with the rest, this method opens a connection with a remote server, so running it synchronously will block the main thread which will lead to server lag.</b>
     * @param uuid
     * @return
     * @throws IOException
     */
    public static String getPlayerName(String uuid) throws IOException {
        String response = getRawJsonResponse(new URL(String.format(GET_NAME_URL, uuid)));
        JsonObject o = JSON_PARSER.fromJson(response, JsonObject.class);
        if (o == null)
            return null;
        return o.get("name") == null ? null : o.get("name").getAsString();
    }

    /**
     * This is a helper method used to read the response of Mojang's API webservers.
     * @param u the URL to connect to
     * @return a String with the data read.
     * @throws IOException Inherited by {@link BufferedReader#readLine()}, {@link BufferedReader#close()}, {@link URL}, {@link HttpURLConnection#getInputStream()}
     */
    private static String getRawJsonResponse(URL u) throws IOException {
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setDoInput(true);
        con.setConnectTimeout(2000);
        con.setReadTimeout(2000);
        con.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String response = in.readLine();
        in.close();
        return response;
    }

    /**
     * This class represents the typical response expected by Mojang servers when requesting the name history of a player.
     */
    public class PreviousPlayerNameEntry {
        private String name;
        @SerializedName("changedToAt")
        private long changeTime;

        /**
         * Gets the player name of this entry.
         * @return The name of the player.
         */
        public String getPlayerName() {
            return name;
        }

        /**
         * Get the time of change of the name.
         * <br><b>Note: This will return 0 if the name is the original (initial) name of the player! Make sure you check if it is 0 before handling!
         * <br>Parsing 0 to a Date will result in the date "01/01/1970".</b>
         * @return a timestamp in miliseconds that you can turn into a date or handle however you want :)
         */
        public long getChangeTime() {
            return changeTime;
        }

        /**
         * Check if this name is the name used to register the account (the initial/original name)
         * @return a boolean, true if it is the the very first name of the player, otherwise false.
         */
        public boolean isPlayersInitialName() {
            return getChangeTime() == 0;
        }

        @Override
        public String toString() {
            return "Name: " + name + " Date of change: " + new Date(changeTime).toString();
        }
    }

}