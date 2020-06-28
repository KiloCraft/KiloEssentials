package org.kilocraft.essentials.extensions.customcommands.config.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.kilocraft.essentials.config.main.sections.PermissionRequirementConfigSection;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class CustomCommandConfigSection {

    @Setting("usage")
    public String usage = "/example <args>...";

    @Setting("label")
    public String label = "example";

    @Setting("runs")
    public List<String> executablesList = new ArrayList<String>(){{
        add("!tellraw ${source.name} {\"text\":\"Example runnable!\"}");
    }};

    @Setting("requires")
    public PermissionRequirementConfigSection reqSection = new PermissionRequirementConfigSection();

}
