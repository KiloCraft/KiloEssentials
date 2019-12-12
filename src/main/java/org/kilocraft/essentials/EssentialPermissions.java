package org.kilocraft.essentials;

public enum EssentialPermissions {
    STAFF("staff"),
    CHAT_COLOR("chat.color"),
    CHAT_BYPASS("chat.bypass"),
    CHAT_GET_PINGED("chat.ping.get_pinged"),
    CHAT_PING_OTHER("chat.ping.other"),
    CHAT_PING_EVERYONE("chat.ping.everyone"),
    ;

    private String node;
    private EssentialPermissions(String node) {
        this.node = node;
    }

    public String getNode() {
        return this.node;
    }
}
