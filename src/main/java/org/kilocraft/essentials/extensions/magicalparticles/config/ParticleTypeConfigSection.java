package org.kilocraft.essentials.extensions.magicalparticles.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.kilocraft.essentials.config.main.sections.PermissionRequirementConfigSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ConfigSerializable
public class ParticleTypeConfigSection {

    @Setting("name")
    public String name = "Breath of the Dragon";

    @Setting("frames")
    public List<ParticleFrameConfigSection> frames = new ArrayList<ParticleFrameConfigSection>() {{
        add(new ParticleFrameConfigSection());
    }};

    @Setting("requires")
    private PermissionRequirementConfigSection permissionRequirement = null;

    public Optional<PermissionRequirementConfigSection> permissionRequirement() {
        return Optional.ofNullable(permissionRequirement);
    }
}
