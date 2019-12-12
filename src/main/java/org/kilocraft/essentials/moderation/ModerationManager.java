package org.kilocraft.essentials.moderation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModerationManager {
    Map<Map<String, UUID>, String> ban_ips = new HashMap<>();
    Map<Map<String, UUID>, Map<String, Date>> tempban_ips = new HashMap<>();
    Map<String, UUID> bans = new HashMap<>();
    Map<Map<String, UUID>, Date> tempbans = new HashMap<>();
    Map<String, UUID> mutes = new HashMap<>();
    Map<Map<String, UUID>, Date> tempmutes = new HashMap<>();

    public ModerationManager() {
    }


    public boolean isIpBanned(UUID uuid) {
        return true;
    }

}
