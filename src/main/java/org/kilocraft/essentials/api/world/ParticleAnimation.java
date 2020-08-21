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
        return id;
    }

    public String getName() {
        return name;
    }

    @NotNull
    public Predicate<OnlineUser> predicate() {
        return predicate == null ? user -> true : predicate;
    }

    public void setPredicate(@Nullable final Predicate<OnlineUser> predicate) {
        this.predicate = predicate;
    }

    public ParticleAnimation append(ParticleAnimationSection<?> frame) {
        particleAnimationSections.add(frame);
        return this;
    }

    public List<ParticleAnimationSection<?>> getFrames() {
        return particleAnimationSections;
    }

    public int frames() {
        return particleAnimationSections.size();
    }
}
