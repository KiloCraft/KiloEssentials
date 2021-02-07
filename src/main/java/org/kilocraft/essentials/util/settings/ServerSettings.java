package org.kilocraft.essentials.util.settings;

import net.minecraft.SharedConstants;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.servermeta.PlayerListMeta;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;
import org.kilocraft.essentials.util.settings.values.*;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ServerSettings implements NBTStorage {

    public static IntegerSetting VIEWDISTANCE = (IntegerSetting) new IntegerSetting(10, "view_distance").onChanged(distance -> KiloEssentials.getServer().getMinecraftServer().getPlayerManager().setViewDistance(distance));
    public static IntegerSetting TICK_DISTANCE = new IntegerSetting(10, "tick_distance");
    public static DoubleSetting SHULKER_SPAWN_CHANCE = new DoubleSetting(0D, "shulker_spawn_chance");
    public static DoubleSetting ITEM_MERGE_RADIUS = new DoubleSetting(0.5D, "item_merge_radius").range(0D, 256D);
    public static IntegerSetting GUARDIAN_SPAWN_CHECK_LIMIT = new IntegerSetting(-1, "guardian_spawn_check_limit");
    public static IntegerSetting GUARDIAN_SPAWN_CHECK_RANGE = new IntegerSetting(-1, "guardian_spawn_check_range");
    public static IntegerSetting DRIP_LEAF_HEIGHT = new IntegerSetting(5, "drip_leaf_height");
    public static BooleanSetting DEBUG = (BooleanSetting) new BooleanSetting(false, "debug").onChanged(b -> SharedConstants.isDevelopment = b);
    public static BooleanSetting TICK_ENTITIES = new BooleanSetting(true, "tick_entities");
    public static BooleanSetting TICK_VILLAGERS = new BooleanSetting(true, "tick_villagers");
    private IntegerSetting wither_check_distance = new IntegerSetting(2, "wither_check_distance").range(-256, 256);
    private DoubleSetting wither_tp_distance = new DoubleSetting(0.5, "wither_tp_distance").range(-256D, 256D);

    public ServerSettings() {
        NBTStorageUtil.addCallback(this);
        KiloCommands.getInstance().register(new SettingCommand());
    }

    public int getInteger(String id) throws InvalidKeyException {
        for (AbstractSetting<?> setting : AbstractSetting.getValueList()) {
            if (setting instanceof IntegerSetting && setting.getId().equals(id)) {
                return ((IntegerSetting) setting).getValue();
            }
        }
        throw new InvalidKeyException();
    }

    public float getFloat(String id) throws InvalidKeyException {
        for (AbstractSetting<?> setting : AbstractSetting.getValueList()) {
            if (setting instanceof FloatSetting && setting.getId().equals(id)) {
                return ((FloatSetting) setting).getValue();
            }
        }
        throw new InvalidKeyException();
    }

    public double getDouble(String id) throws InvalidKeyException {
        for (AbstractSetting<?> setting : AbstractSetting.getValueList()) {
            if (setting instanceof DoubleSetting && setting.getId().equals(id)) {
                return ((DoubleSetting) setting).getValue();
            }
        }
        throw new InvalidKeyException();
    }

    public Object getValue(String id) throws InvalidKeyException {
        return getSetting(id).getValue();
    }

    public AbstractSetting<?> getSetting(String id) throws InvalidKeyException {
        for (AbstractSetting<?> setting : AbstractSetting.getValueList()) {
            if (setting.getId().equals(id)) {
                return setting;
            }
        }
        throw new InvalidKeyException();
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
        for (AbstractSetting<?> setting : AbstractSetting.getValueList()) {
            setting.toTag(tag);
        }
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
        for (AbstractSetting<?> setting : AbstractSetting.getValueList()) {
            setting.fromTag(tag);
        }
    }

}
