package org.kilocraft.essentials.extensions.magicalparticles.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

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
    public LineConfigSection lineConfigSection = this.shape.equals("line") ? new LineConfigSection() : null;

    @Setting("bezierProperties")
    public BezierConfigSection bezierConfigSection = this.shape.equals("bezier") ? new BezierConfigSection() : null;

    public Optional<LineConfigSection> getLineConfigSection() {
        return Optional.ofNullable(this.lineConfigSection);
    }

    public Optional<BezierConfigSection> getBezierConfigSection() {
        return Optional.ofNullable(this.bezierConfigSection);
    }

}
