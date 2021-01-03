package org.kilocraft.essentials.util.settings;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;

import java.util.*;

public class ServerSettingManager implements NBTStorage {

    private Value<Float> GLOBAL_MOBCAP = new Value<>(1F);
    private Value<Float> AMBIENT_MOBCAP = new Value<>(1F);
    private Value<Float> CREATURE_MOBCAP = new Value<>(1F);
    private Value<Float> MISC_MOBCAP = new Value<>(1F);
    private Value<Float> MONSTER_MOBCAP = new Value<>(1F);
    private Value<Float> WATER_AMBIENT_MOBCAP = new Value<>(1F);
    private Value<Float> WATER_CREATURE_MOBCAP = new Value<>(1F);
    private Value<Integer> viewDistance = new Value<>(10);
    private HashMap<SpawnGroup, Value<Float>> spawnGroupToPreference = new HashMap<>();

    public ServerSettingManager() {
        NBTStorageUtil.addCallback(this);
        spawnGroupToPreference.put(SpawnGroup.AMBIENT, AMBIENT_MOBCAP);
        spawnGroupToPreference.put(SpawnGroup.CREATURE, CREATURE_MOBCAP);
        spawnGroupToPreference.put(SpawnGroup.MISC, MISC_MOBCAP);
        spawnGroupToPreference.put(SpawnGroup.MONSTER, MONSTER_MOBCAP);
        spawnGroupToPreference.put(SpawnGroup.WATER_AMBIENT, WATER_AMBIENT_MOBCAP);
        spawnGroupToPreference.put(SpawnGroup.WATER_CREATURE, WATER_CREATURE_MOBCAP);
    }

    public float getMultiplier(Identifier world, SpawnGroup spawnGroup) {
        return SpawnGroupValue.get(world, spawnGroup).getValue();
    }

    public void setMultiplier(Identifier world, SpawnGroup spawnGroup, float multiplier) {
        SpawnGroupValue.get(world, spawnGroup).setValue(multiplier);
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("settings.dat", KiloEssentials.getDataDirPath());
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        CompoundTag mobcaps = new CompoundTag();
        for (Map.Entry<Identifier, List<SpawnGroupValue>> entry : SpawnGroupValue.byWorld.entrySet()) {
            CompoundTag world = new CompoundTag();
            for (SpawnGroupValue value : entry.getValue()) {
                world.putFloat(value.getID(), value.getValue());
            }
            mobcaps.put(entry.getKey().toString(), world);
        }
        tag.put("mobcaps", mobcaps);
        tag.putInt("view_distance", viewDistance.getValue());
        return tag;
    }

    @Override
    public void deserialize(@NotNull CompoundTag tag) {
        if (tag.contains("mobcaps")) {
            CompoundTag mobcaps = tag.getCompound("mobcaps");
            for (String worldKey : mobcaps.getKeys()) {
                Identifier id = new Identifier(worldKey);
                for (String group : mobcaps.getCompound(worldKey).getKeys()) {
                    Float f = mobcaps.getCompound(worldKey).getFloat(group);
                    List<SpawnGroup> values = new ArrayList<>(Arrays.asList(SpawnGroup.values()));
                    values.add(null);
                    for (SpawnGroup value : values) {
                        if (SpawnGroupValue.getID(value).equals(group)) {
                            new SpawnGroupValue(id, value, f);
                        }
                    }
                }
            }
        }
        if (tag.contains("view_distance")) viewDistance.setValue(tag.getInt("view_distance"));
        KiloEssentials.getServer().getMinecraftServer().getPlayerManager().setViewDistance(viewDistance.getValue());
    }

    public int getViewDistance() {
        return viewDistance.getValue();
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance.setValue(viewDistance);
    }
}
