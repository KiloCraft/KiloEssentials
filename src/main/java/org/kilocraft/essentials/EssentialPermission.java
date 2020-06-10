package org.kilocraft.essentials;

import org.kilocraft.essentials.util.PermissionUtil;

public enum EssentialPermission {
    STAFF("staff"),
    BUILDER("builder"),
    CHAT_COLOR("chat.color"),
    CHAT_URL("chat.url"),
    CHAT_SHOW_ITEM("chat.show_item"),
    CHAT_BYPASS("chat.bypass"),
    CHAT_GET_PINGED("chat.ping.get_pinged"),
    CHAT_PING_OTHER("chat.ping.other"),
    CHAT_PING_EVERYONE("chat.ping.everyone"),
    SIGN_COLOR("sign.color"),
    ANVIL_COLOR("anvil.color"),
    SPY_CHAT("spy.chat"),
    SPY_COMMAND("spy.command"),
    SERVER_MANAGE_STOP("server.manage.stop"),
    SERVER_MANAGE_RESTART("server.manage.restart"),
    SERVER_MANAGE_OPERATORS("server.manage.operators"),
    SERVER_MANAGE_MOTD("server.manage.motd"),
    CHAT_CHANNEL_STAFFMSG("chat.channel.staff"),
    CHAT_CHANNEL_BUILDERMSG("chat.channel.builder"),
    RTP_SELF("rtp.self"),
    RTP_OTHERS("rtp.others"),
    RTP_BYPASS("rtp.bypass"),
    RTP_OTHERDIMENSIONS("rtp.otherdimensions"),
    RTP_MANAGE("rtp.manage"),
    MAGIC_PARTICLES_SELF("magicparticles"),
    MAGIC_PARTICLES_ADVANCED("magicparticles.advanced"),
    SIT_SELF("sit.self"),
    SIT_OTHERS("sit.others"),
    IGNORE_COMMAND_EVENTS("ignore_command_events");

    private final String node;

    EssentialPermission(String node) {
        this.node = node;
    }

    public String getNode() {
        return PermissionUtil.PERMISSION_PREFIX + this.node;
    }

    public static EssentialPermission byName(String name) {
        for (EssentialPermission value : EssentialPermission.values()) {
            if (value.node.equals(name.toUpperCase())) {
                return value;
            }
        }

        return null;
    }
}
