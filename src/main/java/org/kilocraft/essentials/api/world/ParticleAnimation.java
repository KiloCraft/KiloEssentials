package org.kilocraft.essentials.api.world;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ParticleAnimation {
    private List<ParticleFrame> particleFrames;
    private Identifier id;

    public ParticleAnimation(Identifier id) {
        this.particleFrames = new ArrayList<>();
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    public ParticleAnimation append(ParticleFrame frame) {
        particleFrames.add(frame);
        return this;
    }

    public List<ParticleFrame> getFrames() {
        return particleFrames;
    }

    public int frames() {
        return particleFrames.size();
    }
}
