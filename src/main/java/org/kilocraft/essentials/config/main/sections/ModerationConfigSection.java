package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ModerationConfigSection {

    @Setting("defaults")
    private Defaults defaults = new Defaults();

    @Setting("meta")
    private Meta meta = new Meta();

    public Defaults defaults() {
        return defaults;
    }

    public Meta meta() {
        return meta;
    }

    @ConfigSerializable
    public static class Defaults {
        @Setting(value = "ban", comment = "The Default ban reason")
        public String ban = "Banned by an operator";

        @Setting(value = "kick", comment = "The Default kick reason")
        public String kick = "Kicked by an operator";
    }

    @ConfigSerializable
    public static class Meta {

    }

}
