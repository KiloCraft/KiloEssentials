package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ModerationConfigSection {

    @Setting("defaults")
    private Defaults defaults = new Defaults();

    @Setting("disconnectReasons")
    private DisconnectReasons disconnectReasons = new DisconnectReasons();

    @Setting("meta")
    private Meta meta = new Meta();

    public Defaults defaults() {
        return defaults;
    }
    public DisconnectReasons disconnectReasons() {
        return disconnectReasons;
    }
    public Meta meta() {
        return meta;
    }

    @ConfigSerializable
    public static class Defaults {
        @Setting(value = "kick", comment = "The Default kick reason")
        public String kick = "Kicked by an operator";
    }

    @ConfigSerializable
    public static class DisconnectReasons {
        @Setting(value = "permban", comment = "Disconnect message for permanent bans")
        public String permBan = "&c&lYou have been banned\n\n&cReason: &f{BAN_REASON}\n&cBy: &f{BAN_SOURCE}\n\n&9Appeal at: &fdiscord.gg/uzuQEe9";

        @Setting(value = "tempban", comment = "Disconnect message for temporary ip-bans")
        public String tempBan = "&c&lYou have been banned\n\n&cReason: &f{BAN_REASON} \n&cUntil: &f{BAN_EXPIRY}\n&cBy: &f{BAN_SOURCE}\n\n&9Appeal at: &fdiscord.gg/uzuQEe9";

        @Setting(value = "permipban", comment = "Disconnect message for permanent bans")
        public String permIpBan = "&c&lYou have been ip-banned\n\n&cReason: &f{BAN_REASON}\n&cBy: &f{BAN_SOURCE}\n\n&9Appeal at: &fdiscord.gg/uzuQEe9";

        @Setting(value = "tempipban", comment = "Disconnect message for temporary ip-bans")
        public String tempIpBan = "&c&lYou have been ip-banned\n\n&cReason: &f{BAN_REASON} \n&cUntil: &f{BAN_EXPIRY}\n&cBy: &f{BAN_SOURCE}\n\n&9Appeal at: &fdiscord.gg/uzuQEe9";
    }

    @ConfigSerializable
    public static class Meta {

    }

}
