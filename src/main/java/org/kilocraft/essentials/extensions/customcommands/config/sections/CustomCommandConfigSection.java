package org.kilocraft.essentials.extensions.customcommands.config.sections;

import org.kilocraft.essentials.config.main.sections.PermissionRequirementConfigSection;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class CustomCommandConfigSection {

    @Setting("usage")
    public String usage = "/example <args>...";

    @Setting("label")
    public String label = "example";

    @Setting("runs")
    public List<String> executablesList = new ArrayList<>() {{
        this.add("!tellraw ${source.name} {\"text\":\"Example runnable!\"}");
    }};

    @Setting("requires")
    public PermissionRequirementConfigSection reqSection = new PermissionRequirementConfigSection();

}
