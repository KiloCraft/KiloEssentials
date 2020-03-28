package org.kilocraft.essentials.extensions.rankmanager.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class RankPermissionsConfigSection {

    @Setting(value = "allowed", comment = "Contains the permission nodes that this rank can use")
    private List<String> allowed = new ArrayList<String>(){{
        this.add("kiloessentials.chat.ping.get_pinged");
        this.add("kiloessentials.chat.ping.other");
        this.add("kiloessentials.rtp.self");
        this.add("kiloessentials.command.home.self");
        this.add("kiloessentials.command.homes.self");
        this.add("kiloessentials.command.home.limit.3");
        this.add("kiloessentials.command.ping.self");
        this.add("kiloessentials.command.warp");
        this.add("kiloessentials.sit.self");
        this.add("kiloessentials.command.nickname.self");
    }};

    @Setting(value = "disallowed", comment = "Contains the permission nodes that this rank can not use")
    private List<String> disallowed = new ArrayList<String>(){{
        this.add("kiloessentials.command.fly.self");
    }};

    public List<String> getAllowed() {
        return allowed;
    }

    public List<String> getDisallowed() {
        return disallowed;
    }

}
