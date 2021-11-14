package org.kilocraft.essentials.api.world;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ParticleAnimation {
    private final List<ParticleAnimationSection<?>> particleAnimationSections;
    private final Identifier id;
    private final String name;
    private Predicate<OnlineUser> predicate;

    public ParticleAnimation(Identifier id, String name) {
        this(id, name, null);
    }

    public ParticleAnimation(@NotNull final Identifier id, @NotNull final String name, @Nullable Predicate<OnlineUser> predicate) {
        this.particleAnimationSections = new ArrayList<>();
        this.id = id;
        this.name = name;
        this.predicate = predicate;
    }

    public Identifier getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public Predicate<OnlineUser> predicate() {
        return this.predicate;
    }

    public boolean canUse(OnlineUser user) {
        if (this.predicate != null) {
            return this.predicate.test(user);
        }
        return true;
    }

    public void setPredicate(@Nullable final Predicate<OnlineUser> predicate) {
        this.predicate = predicate;
    }

    public ParticleAnimation append(ParticleAnimationSection<?> frame) {
        this.particleAnimationSections.add(frame);
        return this;
    }

    public List<ParticleAnimationSection<?>> getFrames() {
        return this.particleAnimationSections;
    }

    public int frames() {
        return this.particleAnimationSections.size();
    }
}
