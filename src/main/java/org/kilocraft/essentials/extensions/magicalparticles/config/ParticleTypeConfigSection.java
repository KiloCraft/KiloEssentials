package org.kilocraft.essentials.extensions.magicalparticles.config;

import org.kilocraft.essentials.config.main.sections.PermissionRequirementConfigSection;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ConfigSerializable
public class ParticleTypeConfigSection {

    @Setting("name")
    public String name = "Breath of the Dragon";

    @Setting("frames")
    public List<ParticleFrameConfigSection> frames = new ArrayList<>() {{
        this.add(new ParticleFrameConfigSection());
    }};

    @Setting("requires")
    private final PermissionRequirementConfigSection permissionRequirement = new PermissionRequirementConfigSection();

    public PermissionRequirementConfigSection permissionRequirement() {
        return this.permissionRequirement;
    }
}
