package org.kilocraft.essentials.extensions.customcommands.config.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class CCArgConfigSection {

    @Setting("id")
    public String id = "example-argument";

    @Setting("requires")
    public CCReqConfigSection reqSection = new CCReqConfigSection();

    @Setting("runs")
    public List<String> executablesList = new ArrayList<String>(){{
        add("!tellrw ${source.name} {\"text\":\"Example Argument!\"}");
    }};

    @Setting("args")
    public List<CCArgConfigSection> args = new ArrayList<>();

}
