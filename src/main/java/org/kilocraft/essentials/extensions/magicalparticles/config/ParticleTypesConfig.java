package org.kilocraft.essentials.extensions.magicalparticles.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config.main.Config;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class ParticleTypesConfig {
    public static String HEADER = Config.HEADER + "\n\nParticle Types:\n AMBIENT_ENTITY_EFFECT, ANGRY_VILLAGER, BARRIER, BLOCK, BUBBLE, CLOUD, CRIT, DAMAGE_INDICATOR,\n" +
            "DRAGON_BREATH, DRIPPING_LAVA, FALLING_LAVA, LANDING_LAVA, DRIPPING_WATER, FALLING_WATER, DUST, EFFECT, ELDER_GUARDIAN,\n" +
            "ENCHANT, END_ROD, ENTITY_EFFECT, EXPLOSION_EMITTER, EXPLOSION, FALLING_DUST, FIREWORK, FISHING, FLAME, FLASH\n" +
            "HAPPY_VILLAGER, COMPOSTER, HEART, INSTANT_EFFECT, ITEM, ITEM_SLIME, ITEM_SNOWBALL, LARGE_SMOKE, LAVA, MYCELIUM, NOTE\n" +
            "POOF, PORTAL, RAIN, SMOKE, SNEEZE, SPIT, SQUID_INK, SWEEP_ATTACK, TOTEM_OF_UNDYING, UNDERWATER, SPLASH, WITCH\n" +
            "BUBBLE_POP, CURRENT_DOWN, BUBBLE_COLUMN_UP, NAUTILUS, DOLPHIN, CAMPFIRE_COSY_SMOKE, CAMPFIRE_SIGNAL_SMOKE\n" +
            "DRIPPING_HONEY, FALLING_HONEY, LANDING_HONEY, FALLING_NECTAR, ENCHANTED_HIT, ASH, CRIMSON_SPORE, WARPED_SPORE, SOUL_FIRE_FLAME" +
            "\n\nNote: Some Particles have special properties! Like \"block, item, dust, falling_dust\"" +
            "\nSpecial property for the Type \"block\" and \"item\": blockProperties { id=\"(A Block Identifier)\" }" +
            "\nSpecial property for the Type \"dust\" and \"falling_dust\": dustProperties { rgb=[0, 0, 0] scale=1.0F }";

    @Setting
    public Map<String, ParticleTypeConfigSection> types = new HashMap<String, ParticleTypeConfigSection>() {{
        put("default:dragon_breath", new ParticleTypeConfigSection());
    }};

    @Setting(value = "pps", comment = "Particle per second, you can set how fast you want the particles to show up\n" +
            "Recommended (Default): 4, the Value must be between 0 to 20")
    private int pps = 4;

    public int getPps() {
        if (pps < 0 || pps > 20) {
            pps = 4;
            KiloEssentials.getLogger().error("Exception while loading the Particle Types, the \"pps\" Value must be between 0 to 20");
        }

        return pps;
    }

}
