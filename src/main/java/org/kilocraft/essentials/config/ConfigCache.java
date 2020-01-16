package org.kilocraft.essentials.config;

import com.electronwill.nightconfig.core.file.FileConfig;

import static org.kilocraft.essentials.config.KiloConfig.MAIN;
import static org.kilocraft.essentials.config.KiloConfig.MESSAGES;

@SuppressWarnings("")
public enum  ConfigCache {
    SERVER_NAME("server.name", "general"),
    SERVER_DISPLAY_BRANDNAME("server.displayBrandName", "general"),
    CHAT_PING_FORMAT_EVERYONE("chat.ping.format_everyone", "general"),
    CHAT_PING_FORMAT("chat.ping.format", "general"),
    CHAT_PING_PINGED("chat.ping.pinged", "general"),
    CHAT_PING_NOT_PINGED("chat.ping.not_pinged", "general"),
    CHAT_PINGED_EVERYONE("chat.ping.pinged_everyone", "general"),
    CHAT_PING_SOUND_ENABLED("chat.ping.sound.enable", "general"),
    CHAT_CHANNELS_FORMATS_GLOBAL("chat.channels.formats.global", "general"),
    CHAT_CHANNELS_FORMATS_STAFF("chat.channels.formats.staff", "general"),
    CHAT_CHANNELS_FORMATS_BUILDER("chat.channels.formats.builder", "general"),
    CHAT_PING_ENABLED("chat.ping.enable", "general"),
    COMMANDS_CONTEXT_EXECUTION_EXCEPTION("commands.context.execution_exception", "messages"),
    COMMANDS_CONTEXT_PERMISSION_EXCEPTION("commands.context.permission_exception", "messages"),
    COMMANDS_SUGGESTIONS_REQUIRE_PERMISSION("commands.suggestions.require_permission", "general"),
    DISABLE_EVENT_MESSAGES_ON_BUNGEE_MODE("events.disable-on-bungee-mode", "messages"),
    BUNGEECORD_MODE("server.bungeecord-mode", "general"),
    USE_VANILLA_CHAT("chat.use-vanilla-chat-instead", "general"),
    KICK_IF_ILLEGAL_CHARACTERS("chat.kick-if-illegal-characters", "general"),
    ;

    private String key;
    private String config;
    private Object value;

    ConfigCache(String key, String config) {
        this.key = key;
        this.config = config;
    }

    static void load() {
        for (ConfigCache it : values()) {
            it.value = getFile(it).getOrElse(it.key, "NULL<Cache>");
        }
    }

    private static FileConfig getFile(ConfigCache c) {
        return c.config.equals("general") ? MAIN : MESSAGES;
    }

    public String getKey() {
        return this.key;
    }

    public Object getValue() {
        return this.value;
    }

}