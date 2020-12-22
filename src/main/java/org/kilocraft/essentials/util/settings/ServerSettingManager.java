package org.kilocraft.essentials.util.settings;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;

import java.util.HashMap;
import java.util.Map;

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

    public float getMultiplier(SpawnGroup spawnGroup) {
        if (spawnGroupToPreference.containsKey(spawnGroup)) return spawnGroupToPreference.get(spawnGroup).getValue();
        return 1;
    }

    public void setMutliplier (SpawnGroup spawnGroup, float multiplier) {
        spawnGroupToPreference.get(spawnGroup).setValue(multiplier);
    }

    public void setMultiplier(float multiplier) {
        GLOBAL_MOBCAP.setValue(multiplier);
    }

    public float getMultiplier() {
        return GLOBAL_MOBCAP.getValue();
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("settings.dat", KiloEssentials.getDataDirPath());
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        CompoundTag mobcaps = new CompoundTag();
        mobcaps.putFloat("global", GLOBAL_MOBCAP.getValue());
        for (Map.Entry<SpawnGroup, Value<Float>> entry : spawnGroupToPreference.entrySet()) {
            mobcaps.putFloat(entry.getKey().asString(), entry.getValue().getValue());
        }
        tag.put("mobcaps", mobcaps);
        tag.putInt("view_distance", viewDistance.getValue());
        return tag;
    }

    @Override
    public void deserialize(@NotNull CompoundTag tag) {
        if (tag.contains("mobcaps")) {
            CompoundTag mobcaps = tag.getCompound("mobcaps");
            if (mobcaps.contains("global")) GLOBAL_MOBCAP.setValue(mobcaps.getFloat("global"));
            for (Map.Entry<SpawnGroup, Value<Float>> entry : spawnGroupToPreference.entrySet()) {
                if (mobcaps.contains(entry.getKey().asString())) entry.getValue().setValue(mobcaps.getFloat(entry.getKey().asString()));
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
