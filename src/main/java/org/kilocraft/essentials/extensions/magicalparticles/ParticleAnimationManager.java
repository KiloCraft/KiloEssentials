package org.kilocraft.essentials.extensions.magicalparticles;

import com.google.common.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.world.ParticleAnimation;
import org.kilocraft.essentials.api.world.ParticleFrame;
import org.kilocraft.essentials.api.world.RelativePosition;
import org.kilocraft.essentials.extensions.magicalparticles.config.BlockStateParticleEffectConfigSection;
import org.kilocraft.essentials.extensions.magicalparticles.config.DustParticleEffectConfigSection;
import org.kilocraft.essentials.extensions.magicalparticles.config.ParticleFrameConfigSection;
import org.kilocraft.essentials.extensions.magicalparticles.config.ParticleTypesConfig;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.NBTStorageUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ParticleAnimationManager implements ConfigurableFeature, NBTStorage {
    static Map<Identifier, ParticleAnimation> map = new HashMap<>();
    private static Map<UUID, Identifier> uuidIdentifierMap = new HashMap<>();
    private static ParticleTypesConfig config;

    @Override
    public boolean register() {
        NBTStorageUtil.addCallback(this);
        KiloEssentials.getInstance().getCommandHandler().register(new MagicalParticlesCommand());
        load();
        return true;
    }

    public static void load() {
        loadConfig();
        createFromConfig();
    }

    private static void loadConfig() {
        try {
            KiloFile CONFIG_FILE = new KiloFile("particleTypes.hocon", KiloEssentials.getEssentialsPath());
            if (!CONFIG_FILE.exists()) {
                CONFIG_FILE.createFile();
                CONFIG_FILE.pasteFromResources("assets/config/particleTypes.hocon");
            }

            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                    .setFile(CONFIG_FILE.getFile()).build();

            ConfigurationNode configNode = loader.load(ConfigurationOptions.defaults()
                    .setHeader(ParticleTypesConfig.HEADER)
                    .setObjectMapperFactory(DefaultObjectMapperFactory.getInstance())
                    .setShouldCopyDefaults(true));

            config = configNode.getValue(TypeToken.of(ParticleTypesConfig.class), new ParticleTypesConfig());

            loader.save(configNode);
        } catch (IOException | ObjectMappingException e) {
            KiloEssentials.getLogger().error("Exception handling a configuration file! " + ParticleAnimationManager.class.getName());
            e.printStackTrace();
        }
    }

    private static void createFromConfig() {
        map.clear();
        config.types.forEach((string, innerArray) -> {
            ParticleAnimation animation = new ParticleAnimation(new Identifier(string.toLowerCase()), innerArray.name);

            for (int i = 0; i < innerArray.frames.size(); i++) {
                ParticleFrameConfigSection frame = innerArray.frames.get(i);
                ParticleType<?> effect = ParticleFrame.getEffectByName(frame.effect);

                if (effect == null) {
                    KiloEssentials.getLogger().error("Error identifying the Particle type while loading ParticleTypes!" +
                            " Entered id \"" + frame.effect + "\" is not a valid ParticleEffect!");
                    continue;
                }

                String[] oI = frame.offset.split(" ");
                String[] pI = frame.pos.split(" ");
                double offsetX = Double.parseDouble(oI[0]);
                double offsetY = Double.parseDouble(oI[1]);
                double offsetZ = Double.parseDouble(oI[2]);
                double x = Double.parseDouble(pI[0]);
                double y = Double.parseDouble(pI[1]);
                double z = Double.parseDouble(pI[2]);

                ParticleEffect particleEffect = null;

                if (frame.getBlockStateSection().isPresent() && !frame.getDustParticleSection().isPresent()) {
                    BlockStateParticleEffectConfigSection section = frame.getBlockStateSection().get();
                    Block block = Registry.BLOCK.get(new Identifier(section.blockId.toLowerCase()));

                    if (block == Blocks.AIR && Registry.BLOCK.getDefaultId().getPath().equalsIgnoreCase(section.blockId)) {
                        KiloEssentials.getLogger().warn("Error when initializing a ParticleFrame! Id: " + string +
                                " Frame: " + i + ". Default block id \"air\" found! The entered block id " + section.blockId +
                                "  is wrong!");
                    }

                    particleEffect = new BlockStateParticleEffect(
                            ParticleTypes.BLOCK,
                            Registry.BLOCK.get(new Identifier(frame.getBlockStateSection().get().blockId)).getDefaultState()
                    );

                } else if (frame.getDustParticleSection().isPresent() && !frame.getBlockStateSection().isPresent()) {
                    DustParticleEffectConfigSection section = frame.getDustParticleSection().get();

                    boolean shouldContinue = true;
                    for (int i1 = 0; i1 < section.rgb.size(); i1++) {
                        int color = section.rgb.get(i1);
                        if (color > 255 || color < 0) {
                            KiloEssentials.getLogger().warn("Error when initializing a ParticleFrame! Id: " + string +
                                    " Frame: " + i + "RGB: " + i1 + " Invalid RGB Color value! a RGB Color value must be between 0 and 255");
                            shouldContinue = false;
                        }
                    }

                    if (shouldContinue)
                        particleEffect = new DustParticleEffect(
                                section.rgb.get(0), section.rgb.get(1), section.rgb.get(2), section.scale
                        );
                } else {
                    particleEffect = (DefaultParticleType) effect;
                }

                if (particleEffect != null) {
                    animation.append(new ParticleFrame<>(
                            particleEffect,
                            frame.longDistance,
                            new RelativePosition(x, y, z),
                            offsetX, offsetY, offsetZ,
                            frame.speed, frame.count)
                    );
                } else {
                    KiloEssentials.getLogger().error("Error when initializing a ParticleFrame! Id: " + string +
                            " Frame: " + i);
                }
            }

            map.put(animation.getId(), animation);
        });
    }

    public static void addPlayer(UUID player, Identifier identifier) {
        uuidIdentifierMap.remove(player, identifier);
        uuidIdentifierMap.put(player, identifier);
    }

    public static void removePlayer(UUID player) {
        uuidIdentifierMap.remove(player);
    }

    public static boolean hasParticleAnimation(UUID player) {
        return uuidIdentifierMap.containsKey(player);
    }

    public static boolean isValidId(Identifier id) {
        return map.containsKey(id);
    }

    public static Identifier getIdFromPath(String path) {
        AtomicReference<Identifier> identifier = new AtomicReference<>();
        map.forEach((id, am) -> {
            if (id.getPath().equals(path))
                identifier.set(id);
        });

        return identifier.get();
    }

    private static int tick = 0;
    public static void onTick() {
        //Tick counter logic, only shows the animations once in 4 ticks
        tick++;
        if (tick > config.getPps() && uuidIdentifierMap != null && !uuidIdentifierMap.isEmpty()) {
            try {
                uuidIdentifierMap.forEach((uuid, id) -> {
                    final ServerPlayerEntity player = KiloServer.getServer().getPlayer(uuid);

                    if (player != null && !player.isSpectator())
                        runAnimationFrames(KiloServer.getServer().getPlayer(uuid), id);
                });
            } catch (Exception e) {
                KiloEssentials.getLogger().error("Exception while processing Magical Particles");
                KiloEssentials.getLogger().error(e.getMessage());
            }

            tick = 0;
        }
    }

    static String getAnimationName(Identifier id) {
        return map.get(id).getName();
    }

    private static void runAnimationFrames(final ServerPlayerEntity player, Identifier id) {
        ParticleAnimation animation = map.get(id);

        if (animation == null) {
            removePlayer(player.getUuid());
            return;
        }

        for (ParticleFrame<?> frame : animation.getFrames()) {
            if (frame == null)
                continue;

            Packet<?> packet = frame.toPacket(player.getPos());
            if (packet != null)
                player.getServerWorld().getChunkManager().sendToNearbyPlayers(player, packet);
        }

        animation.frames();
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("particle_animation_cache.dat", KiloEssentials.getDataDirPath());
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        uuidIdentifierMap.forEach((uuid, identifier) -> tag.putString(uuid.toString(), identifier.toString()));
        return tag;
    }

    @Override
    public void deserialize(@NotNull CompoundTag compoundTag) {
        uuidIdentifierMap.clear();
        for (String key : compoundTag.getKeys()) {
            uuidIdentifierMap.put(UUID.fromString(key), new Identifier(compoundTag.getString(key)));
        }
    }
}
