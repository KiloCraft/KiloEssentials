package org.kilocraft.essentials.extensions.magicalparticles;

import net.minecraft.client.network.packet.ParticleS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.world.ParticleAnimation;
import org.kilocraft.essentials.api.world.ParticleFrame;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.NBTStorageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ParticleAnimationManager implements ConfigurableFeature, NBTStorage {
    static Map<Identifier, ParticleAnimation> map = new HashMap<>();
    private static Map<UUID, Identifier> uuidIdentifierMap = new HashMap<>();

    @Override
    public boolean register() {
        NBTStorageUtil.addCallback(this);
        KiloEssentials.getInstance().getCommandHandler().register(new ParticleAnimationCommand());
        return true;
    }

    public static void registerAnimation(ParticleAnimation animation) {
        map.put(animation.getId(), animation);
    }

    public static ParticleAnimation getAnimation(Identifier id) {
        return map.get(id);
    }

    public static void addPlayer(UUID player, Identifier identifier) {
        uuidIdentifierMap.remove(player);
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

    private static Server server = KiloServer.getServer();
    private static AtomicInteger tick = new AtomicInteger(0);
    public static void onTick() {
        if (uuidIdentifierMap.isEmpty())
            return;

        //Tick counter logic, only shows the animations once in 3 ticks
        tick.getAndIncrement();
        if (tick.get() < 4)
            return;

        uuidIdentifierMap.forEach((uuid, id) -> {
            if (server.getPlayer(uuid) != null)
                runAnimationFrames(server.getPlayer(uuid), id);
        });

        tick.set(0);
    }

    public static void runAnimationFrames(ServerPlayerEntity player, Identifier id) {
        ParticleAnimation animation = map.get(id);

        if (animation == null) {
            removePlayer(player.getUuid());
            return;
        }

        for (ParticleFrame frame : animation.getFrames()) {
            if (frame == null)
                continue;

            ParticleS2CPacket packet = frame.toPacket(player.getPos());
            player.getServerWorld().getChunkManager().sendToNearbyPlayers(player, packet);
        }

        animation.frames();
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("particle_animation_cache.dat", KiloEssentials.getDataDirectory());
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
