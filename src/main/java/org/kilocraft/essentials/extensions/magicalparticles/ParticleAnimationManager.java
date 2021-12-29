package org.kilocraft.essentials.extensions.magicalparticles;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.api.feature.ReloadableConfigurableFeature;
import org.kilocraft.essentials.api.feature.TickListener;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.ParticleAnimation;
import org.kilocraft.essentials.api.world.ParticleAnimationSection;
import org.kilocraft.essentials.api.world.RelativePosition;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.PermissionRequirementConfigSection;
import org.kilocraft.essentials.extensions.magicalparticles.config.*;
import org.kilocraft.essentials.extensions.playtimecommands.config.PlaytimeCommandsConfig;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.util.MapFactories;
import com.mojang.math.Vector3f;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ParticleAnimationManager implements ReloadableConfigurableFeature, TickListener, NBTStorage {
    static Map<ResourceLocation, ParticleAnimation> map = new HashMap<>();
    private static final Map<UUID, ResourceLocation> uuidIdentifierMap = new HashMap<>();
    private static ParticleTypesConfig config;

    @Override
    public boolean register() {
        NBTStorageUtil.addCallback(this);
        KiloCommands.register(new MagicalParticlesCommand());
        return true;
    }

    @Override
    public void load() {
        this.loadConfig();
    }

    public void loadConfig() {
        Path path = KiloEssentials.getEssentialsPath().resolve("particle_types.conf");
        final HoconConfigurationLoader hoconLoader = HoconConfigurationLoader.builder()
                .path(path)
                .build();
        try {
            if (!path.toFile().exists()) {
                InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/config/particle_types.conf");
                assert inputStream != null;
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            }
            final CommentedConfigurationNode rootNode = hoconLoader.load(KiloConfig.configurationOptions().header(ParticleTypesConfig.HEADER));
            config = rootNode.get(ParticleTypesConfig.class, new ParticleTypesConfig());
        } catch (IOException e) {
            KiloEssentials.getLogger().error("Exception handling a configuration file!", e);
        }
        createFromConfig();
    }

    private static void createFromConfig() {
        map.clear();
        config.types.forEach((string, innerArray) -> {
            PermissionRequirementConfigSection requirement = innerArray.permissionRequirement();
            ParticleAnimation animation = new ParticleAnimation(
                    new ResourceLocation(string.toLowerCase()),
                    innerArray.name,
                    (user) -> KiloEssentials.hasPermissionNode(user.getCommandSource(), requirement.permission, requirement.op)
            );

            for (int i = 0; i < innerArray.frames.size(); i++) {
                ParticleFrameConfigSection frame = innerArray.frames.get(i);
                ParticleType<?> effect = ParticleAnimationSection.getEffectByName(frame.effect);

                if (effect == null) {
                    KiloEssentials.getLogger().error(
                            "Error identifying the Particle type while loading ParticleTypes!" +
                                    " Entered id \"{}\" is not a valid ParticleEffect!", frame.effect
                    );

                    continue;
                }

                boolean relative = frame.pos.contains("^");
                frame.pos = frame.pos.replace("^", "");

                String[] oI = frame.offset.split(" ");
                String[] pI = frame.pos.split(" ");
                double offsetX = Double.parseDouble(oI[0]);
                double offsetY = Double.parseDouble(oI[1]);
                double offsetZ = Double.parseDouble(oI[2]);
                double x = Double.parseDouble(pI[0]);
                double y = Double.parseDouble(pI[1]);
                double z = Double.parseDouble(pI[2]);

                ParticleOptions particleEffect = null;

                if (frame.getBlockStateSection().isPresent() && frame.getDustParticleSection().isEmpty()) {
                    BlockStateParticleEffectConfigSection section = frame.getBlockStateSection().get();
                    Block block = Registry.BLOCK.get(new ResourceLocation(section.blockId.toLowerCase()));

                    if (block == Blocks.AIR && Registry.BLOCK.getDefaultKey().getPath().equalsIgnoreCase(section.blockId)) {
                        KiloEssentials.getLogger().warn(
                                "Error while initializing a ParticleSection! Id: \"{}\", Section: {}. " +
                                        "Default block id \"air\" found! The entered block id \"{}\" is wrong!",
                                string, i, section.blockId
                        );
                    }

                    particleEffect = new BlockParticleOption(
                            ParticleTypes.BLOCK,
                            Registry.BLOCK.get(new ResourceLocation(frame.getBlockStateSection().get().blockId)).defaultBlockState()
                    );

                } else if (frame.getDustParticleSection().isPresent() && frame.getBlockStateSection().isEmpty()) {
                    DustParticleEffectConfigSection section = frame.getDustParticleSection().get();
                    String[] rgb = section.rgb.split(" ");

                    boolean shouldContinue = true;
                    for (int j = 0; j < 3; j++) {
                        float color = Float.parseFloat(rgb[j]);
                        if (color > 1 || color < 0) {
                            KiloEssentials.getLogger().warn(
                                    "Error while initializing a ParticleSection! Id: \"{}\", Section: {} RGB: {}. " +
                                            "Invalid RGB Color value! a RGB value must be between 0 and 1",
                                    string, i, j
                            );
                            shouldContinue = false;
                        }
                    }

                    if (shouldContinue)
                        particleEffect = new DustParticleOptions(new Vector3f(
                                Float.parseFloat(rgb[0]), Float.parseFloat(rgb[1]), Float.parseFloat(rgb[2])), section.scale
                        );
                } else {
                    if (effect instanceof SimpleParticleType defaultType) {
                        particleEffect = defaultType;
                    } else {
                        KiloEssentials.getLogger().warn("Error parsing particle effect \"{}\"", frame.effect);
                    }
                }

                if (particleEffect != null) {
                    if (relative) {
                        // Shape
                        ShapeConfigSection section = frame.getShapeSection().get();
                        float spacing = section.spacing;

                        if (!section.shape.equals("square") && !section.shape.equals("circle") && !section.shape.equals("line") && !section.shape.equals("bezier")) {
                            KiloEssentials.getLogger().error("Error when initializing a ParticleFrame! Id: " + string +
                                    " Frame: " + i + "Shape " + section.shape + " Invalid shape! Must be: square, circle, line or bezier");
                            return;
                        }

                        if (section.size < 0.1f) {
                            KiloEssentials.getLogger().error("Error when initializing a ParticleFrame! Id: " + string +
                                    " Frame: " + i + "Size " + section.size + " Invalid size! Must be more than 0.1");
                            return;
                        }

                        if (section.spacing < 0.01f) {
                            KiloEssentials.getLogger().error("Error when initializing a ParticleFrame! Id: " + string +
                                    " Frame: " + i + "Spacing " + section.spacing + " Invalid spacing! Must be more than 0.01");
                            return;
                        }

                        if (section.shape.equals("square")) {
                            // Square
                            for (float t = -section.size / 2; t < section.size / 2; t += spacing) {
                                // Left and right line
                                if (t == -section.size / 2 || t + spacing > section.size / 2) {
                                    for (float u = -section.size / 2; u < section.size / 2; u += spacing) {
                                        animation.append(new ParticleAnimationSection<>(
                                                particleEffect,
                                                frame.longDistance,
                                                new RelativePosition(x + t, y + u, z),
                                                offsetX, offsetY, offsetZ,
                                                frame.speed, frame.count, true)
                                        );
                                    }
                                } else {
                                    // Above
                                    animation.append(new ParticleAnimationSection<>(
                                            particleEffect,
                                            frame.longDistance,
                                            new RelativePosition(x + t, y + section.size / 2, z),
                                            offsetX, offsetY, offsetZ,
                                            frame.speed, frame.count, true)
                                    );

                                    // Below
                                    animation.append(new ParticleAnimationSection<>(
                                            particleEffect,
                                            frame.longDistance,
                                            new RelativePosition(x + t, y - section.size / 2, z),
                                            offsetX, offsetY, offsetZ,
                                            frame.speed, frame.count, true)
                                    );
                                }
                            }
                        } else if (section.shape.equals("circle")) {
                            double circumference = Math.PI * Math.pow(section.size / 2, 2);
                            double angle = 360 / (circumference / spacing);

                            // Circle
                            for (float t = 0; t < 360 + angle; t += angle) {
                                float realT = t;
                                if (realT > 360) {
                                    realT = 360;
                                }

                                double newX = section.size / 2 * Math.cos(realT) - (section.size / 2) * Math.sin(realT);
                                double newY = section.size / 2 * Math.sin(realT) + (section.size / 2) * Math.cos(realT);

                                animation.append(new ParticleAnimationSection<>(
                                        particleEffect,
                                        frame.longDistance,
                                        new RelativePosition(x + newX, y + newY, z),
                                        offsetX, offsetY, offsetZ,
                                        frame.speed, frame.count, true)
                                );
                            }
                        } else if (section.getLineConfigSection().isPresent()) {
                            // Line
                            String[] startPosition = section.getLineConfigSection().get().startPosition.split(" ");
                            float startPositionX = Float.parseFloat(startPosition[0]);
                            float startPositionY = Float.parseFloat(startPosition[1]);
                            float startPositionZ = Float.parseFloat(startPosition[2]);

                            String[] endPosition = section.getLineConfigSection().get().endPosition.split(" ");
                            float endPositionX = Float.parseFloat(endPosition[0]);
                            float endPositionY = Float.parseFloat(endPosition[1]);
                            float endPositionZ = Float.parseFloat(endPosition[2]);

                            double distance = Math.abs(startPositionX - endPositionX) + Math.abs(startPositionY - endPositionY) + Math.abs(startPositionZ - endPositionZ);
                            double step = 1 / distance;

                            for (float t = 0; t <= 1; t += step) {
                                double newX = startPositionX + (endPositionX - startPositionX) * t;
                                double newY = startPositionY + (endPositionY - startPositionY) * t;
                                double newZ = startPositionZ + (endPositionZ - startPositionZ) * t;

                                animation.append(new ParticleAnimationSection<>(
                                        particleEffect,
                                        frame.longDistance,
                                        new RelativePosition(x + newX, y + newY, z + newZ),
                                        offsetX, offsetY, offsetZ,
                                        frame.speed, frame.count, true)
                                );
                            }
                        } else if (section.getBezierConfigSection().isPresent()) {
                            // Bezier curve
                            String[] points = section.getBezierConfigSection().get().points.split(" ");
                            // Relative to points
                            String[] controlPoints = section.getBezierConfigSection().get().controlPoints.split(" ");
                            int amountOfPoints = points.length / 3;
                            int usedControlPoints = 0;

                            // One line at the time
                            for (int j = 0; j < amountOfPoints - 1; j++) {
                                float[] startPoint = new float[]{Float.parseFloat(points[j * 3]), Float.parseFloat(points[j * 3 + 1]), Float.parseFloat(points[j * 3 + 2])};
                                float[] startTangent = new float[]{Float.parseFloat(controlPoints[usedControlPoints * 3]), Float.parseFloat(controlPoints[usedControlPoints * 3 + 1]), Float.parseFloat(controlPoints[usedControlPoints * 3 + 2])};
                                float[] endTangent = new float[]{Float.parseFloat(controlPoints[(usedControlPoints + 1) * 3]), Float.parseFloat(controlPoints[(usedControlPoints + 1) * 3 + 1]), Float.parseFloat(controlPoints[(usedControlPoints + 1) * 3 + 2])};
                                float[] endPoint = new float[]{Float.parseFloat(points[(j + 1) * 3]), Float.parseFloat(points[(j + 1) * 3 + 1]), Float.parseFloat(points[(j + 1) * 3 + 2])};

                                // Tangents are relative
                                startTangent[0] += startPoint[0];
                                startTangent[1] += startPoint[1];
                                startTangent[2] += startPoint[2];

                                endTangent[0] += endPoint[0];
                                endTangent[1] += endPoint[1];
                                endTangent[2] += endPoint[2];

                                float distance = startTangent[0] + Math.abs(endTangent[0] - startTangent[0]) + Math.abs(endTangent[0]); // X
                                distance += startTangent[1] + Math.abs(endTangent[1] - startTangent[1]) + Math.abs(endTangent[1]); // Y
                                distance += startTangent[2] + Math.abs(endTangent[2] - startTangent[2]) + Math.abs(endTangent[2]); // Z

                                float step = 1f / distance;

                                for (float t = 0; t <= 1; t += step) {
                                    double[] newPos = getBezierPoint(startPoint, endPoint, startTangent, endTangent, t);

                                    animation.append(new ParticleAnimationSection<>(
                                            particleEffect,
                                            frame.longDistance,
                                            new RelativePosition(x + newPos[0], y + newPos[1], z + newPos[2]),
                                            offsetX, offsetY, offsetZ,
                                            frame.speed, frame.count, true)
                                    );
                                }

                                usedControlPoints += 2;
                            }
                        }
                    } else {
                        animation.append(new ParticleAnimationSection<>(
                                particleEffect,
                                frame.longDistance,
                                new RelativePosition(x, y, z),
                                offsetX, offsetY, offsetZ,
                                frame.speed, frame.count, false)
                        );
                    }
                } else {
                    KiloEssentials.getLogger().error("Error when initializing a ParticleFrame! Id: " + string +
                            " Frame: " + i);
                }
            }

            map.put(animation.getId(), animation);
        });
    }

    public static double[] getBezierPoint(float[] startPoint, float[] endPoint, float[] startTangent, float[] endTangent, float t) {
        double[] result = new double[3];

        result[0] = Math.pow(1 - t, 3) * startPoint[0] + 3 * t * Math.pow(1 - t, 2) * startTangent[0] + 3 * Math.pow(t, 2) * (1 - t) * endTangent[0] + Math.pow(t, 3) * endPoint[0];
        result[1] = Math.pow(1 - t, 3) * startPoint[1] + 3 * t * Math.pow(1 - t, 2) * startTangent[1] + 3 * Math.pow(t, 2) * (1 - t) * endTangent[1] + Math.pow(t, 3) * endPoint[1];
        result[2] = Math.pow(1 - t, 3) * startPoint[2] + 3 * t * Math.pow(1 - t, 2) * startTangent[2] + 3 * Math.pow(t, 2) * (1 - t) * endTangent[2] + Math.pow(t, 3) * endPoint[2];
        return result;
    }

    public static boolean canUse(final OnlineUser user, final ResourceLocation identifier) {
        ParticleAnimation animation = map.get(identifier);
        return animation.canUse(user);
    }

    public static void addPlayer(UUID player, ResourceLocation identifier) {
        uuidIdentifierMap.remove(player, identifier);
        uuidIdentifierMap.put(player, identifier);
    }

    public static void removePlayer(UUID player) {
        uuidIdentifierMap.remove(player);
    }

    public static boolean hasParticleAnimation(UUID player) {
        return uuidIdentifierMap.containsKey(player);
    }

    public static boolean isValidId(ResourceLocation id) {
        return map.containsKey(id);
    }

    public static ResourceLocation getIdFromPath(String path) {
        AtomicReference<ResourceLocation> identifier = new AtomicReference<>();
        map.forEach((id, am) -> {
            if (id.getPath().equals(path))
                identifier.set(id);
        });

        return identifier.get();
    }

    private static int tick = 0;

    public void onTick() {
        // Tick counter logic, only shows the animations once in 4 ticks
        tick++;
        if (tick > config.getPps() && !uuidIdentifierMap.isEmpty()) {
            try {
                for (Map.Entry<UUID, ResourceLocation> entry : uuidIdentifierMap.entrySet()) {
                    ServerPlayer player = KiloEssentials.getMinecraftServer().getPlayerList().getPlayer(entry.getKey());

                    if (player != null && !player.isSpectator()) {
                        runAnimationFrames(player, entry.getValue());
                    }
                }
            } catch (Exception e) {
                KiloEssentials.getLogger().error("Exception while processing Magical Particles", e);
            }

            tick = 0;
        }
    }

    static String getAnimationName(ResourceLocation id) {
        return map.get(id).getName();
    }

    private static void runAnimationFrames(final ServerPlayer player, ResourceLocation id) {
        ParticleAnimation animation = map.get(id);

        if (animation == null) {
            removePlayer(player.getUUID());
            return;
        }

        for (ParticleAnimationSection<?> frame : animation.getFrames()) {
            if (frame == null) {
                continue;
            }

            Packet<?> packet = frame.toPacket(player.position(), player.yBodyRot);
            if (packet != null) {
                player.getLevel().getChunkSource().broadcastAndSend(player, packet);
            }
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
    public void deserialize(@NotNull CompoundTag NbtCompound) {
        uuidIdentifierMap.clear();
        for (String key : NbtCompound.getAllKeys()) {
            uuidIdentifierMap.put(UUID.fromString(key), new ResourceLocation(NbtCompound.getString(key)));
        }
    }
}
