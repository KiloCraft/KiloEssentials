package org.kilocraft.essentials.extensions.emoticles;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class MagicalParticles {


    public <T extends ParticleEffect> void create(ServerPlayerEntity player,
                                                  T particleEffect,
                                                  boolean longDistance,
                                                  double x,
                                                  double y,
                                                  double z,
                                                  int i, double g, double h, double j, double k) {

    }

    public enum Type {
        FLAMES("flames"),
        GLASS("glass"),
        STORMY_CLOUD("stormy_cloud"),
        DRAGON_BREATH("dragon_breath"),
        BONE_MEAL("bone_meal"),
        ;

        private String name;
        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static Type getByName(String name) {
            for (Type value : values()) {
                if (value.name.equals(name))
                    return value;
            }

            return null;
        }

    }
}
