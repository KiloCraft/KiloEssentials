package org.kilocraft.essentials.extensions.magicalparticles.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
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
            "DRIPPING_HONEY, FALLING_HONEY, LANDING_HONEY, FALLING_NECTAR, ENCHANTED_HIT";

//    @Setting public List<ParticleTypeConfigSection> type = new ArrayList<ParticleTypeConfigSection>(){{
//        add(new ParticleTypeConfigSection());
//     }};

    @Setting
    public Map<String, ParticleTypeConfigSection> types = new HashMap<String, ParticleTypeConfigSection>(){{
        put("default:dragon_breath", new ParticleTypeConfigSection());
    }};


}
