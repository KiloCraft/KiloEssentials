package org.kilocraft.essentials.extensions.rankmanager;

import com.google.common.collect.RangeMap;
import com.google.common.reflect.TypeToken;
import net.minecraft.nbt.CompoundTag;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.api.feature.RelodableConfigurableFeature;
import org.kilocraft.essentials.extensions.magicalparticles.ParticleAnimationManager;
import org.kilocraft.essentials.extensions.magicalparticles.config.ParticleTypesConfig;
import org.kilocraft.essentials.extensions.rankmanager.api.RankMeta;
import org.kilocraft.essentials.extensions.rankmanager.config.RankConfigSection;
import org.kilocraft.essentials.extensions.rankmanager.config.RanksConfig;
import org.kilocraft.essentials.provided.KiloFile;

import java.io.IOException;

public class RankManager implements RelodableConfigurableFeature, NBTStorage {
    private static boolean enabled = false;
    private static RankManager instance = null;
    private static RanksConfig config;

    @Override
    public boolean register() {
        enabled = true;
        instance = this;


        return true;
    }

    @Override
    public void load() {
        try {
            KiloFile CONFIG_FILE = new KiloFile("ranks.hocon", KiloEssentials.getEssentialsPath());
            if (!CONFIG_FILE.exists()) {
                CONFIG_FILE.createFile();
                //CONFIG_FILE.pasteFromResources("assets/config/ranks.hocon");
            }

            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                    .setFile(CONFIG_FILE.getFile()).build();

            ConfigurationNode configNode = loader.load(ConfigurationOptions.defaults()
                    .setHeader(RanksConfig.HEADER)
                    .setObjectMapperFactory(DefaultObjectMapperFactory.getInstance())
                    .setShouldCopyDefaults(true));

            config = configNode.getValue(TypeToken.of(RanksConfig.class), new RanksConfig());

            loader.save(configNode);
        } catch (IOException | ObjectMappingException e) {
            KiloEssentials.getLogger().error("Exception handling a configuration file! " + RanksConfig.class.getName());
            e.printStackTrace();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static RankManager getInstance() {
        return instance;
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("rank_data.dat", KiloEssentials.getDataDirPath());
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();

        return tag;
    }

    @Override
    public void deserialize(@NotNull CompoundTag compoundTag) {

    }

}
