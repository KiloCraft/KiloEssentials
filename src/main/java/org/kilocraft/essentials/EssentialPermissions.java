package org.kilocraft.essentials;

public enum EssentialPermissions {
    STAFF("staff"),
    CHAT_COLOR("chat.color"),
    CHAT_BYPASS("chat.bypass"),
    CHAT_GET_PINGED("chat.ping.get_pinged"),
    CHAT_PING_OTHER("chat.ping.other"),
    CHAT_PING_EVERYONE("chat.ping.everyone"),
    CHAT_SOCIALSPY("chat.socialspy"),
    HOME_SELF_TP("home.self.tp"),
    HOME_SELF_SET("home.self.set"),
    HOME_SELF_REMOVE("home.self.remove"),
    HOMES_SELF("homes.self"),
    HOMES_OTHERS("homes.others"),
    HOME_OTHERS_TP("home.others.tp"),
    HOME_OTHERS_SET("home.others.set"),
    HOME_OTHERS_REMOVE("home.others.remove"),
    HOME_SET_LIMIT("home.set.limit."),
    HOME_SET_LIMIT_BYPASS("home.set.limit.bypass")
    ;

    private String node;
    private EssentialPermissions(String node) {
        this.node = node;
    }

    public String getNode() {
        return this.node;
    }
}
