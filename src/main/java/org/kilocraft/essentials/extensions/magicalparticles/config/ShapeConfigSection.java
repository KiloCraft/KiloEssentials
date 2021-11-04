package org.kilocraft.essentials.extensions.magicalparticles.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Optional;

@ConfigSerializable
public class ShapeConfigSection {

    @Setting("shape")
    public String shape = "square";

    @Setting("size")
    public float size = 1;

    @Setting("spacing")
    public float spacing = 0.2f;

    @Setting("lineProperties")
    public LineConfigSection lineConfigSection = new LineConfigSection();

    @Setting("bezierProperties")
    public BezierConfigSection bezierConfigSection = new BezierConfigSection();

    public Optional<LineConfigSection> getLineConfigSection() {
        return Optional.ofNullable(this.shape.equals("line") ? this.lineConfigSection : null);
    }

    public Optional<BezierConfigSection> getBezierConfigSection() {
        return Optional.ofNullable(this.shape.equals("bezier") ? this.bezierConfigSection : null);
    }

}
