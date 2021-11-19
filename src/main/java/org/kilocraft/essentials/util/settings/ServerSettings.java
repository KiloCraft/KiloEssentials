package org.kilocraft.essentials.util.settings;

import net.minecraft.SharedConstants;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;
import org.kilocraft.essentials.util.registry.RegistryKeyID;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.settings.values.*;
import org.kilocraft.essentials.util.settings.values.util.RootSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerSettings implements NBTStorage {

    // TODO: Figure out a clever way to cache values
    public static RootSetting root = new RootSetting();
    public static int wither_check_distance = 2;
    public static double wither_tp_distance = 1;
    public static float[][] mobcap;
    public static boolean patch_item_merge_adjust_movement = true;
    public static boolean patch_eigencraft_redstone = false;
    public static boolean patch_tnt_duping = true;
    public static int patch_save_interval = 6000;

    public ServerSettings() {
        NBTStorageUtil.addCallback(this);
    }

    public static boolean getBoolean(String id) {
        return ((BooleanSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static double getDouble(String id) {
        return ((DoubleSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static float getFloat(String id) {
        return ((FloatSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static int getInt(String id) {
        return ((IntegerSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static String getString(String id) {
        return ((StringSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static void setBoolean(String id, boolean value) {
        ((BooleanSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static void setDouble(String id, double value) {
        ((DoubleSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static void setFloat(String id, float value) {
        ((FloatSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static void setInt(String id, int value) {
        ((IntegerSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static void setString(String id, String value) {
        ((StringSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static int getViewDistance() {
        return KiloEssentials.getMinecraftServer().getPlayerManager().getViewDistance();
    }

    public static void setViewDistance(int viewDistance) {
        ServerSettings.setInt("view_distance", viewDistance);
    }

    public static void registerSettings() {
        // Custom settings
        BooleanSetting debug = new BooleanSetting(false, "debug").onChanged(b -> SharedConstants.isDevelopment = b);

        // Patches
        CategorySetting patch = new CategorySetting("patch");
        // Shulker dye
        BooleanSetting dye_shulkers = new BooleanSetting(false, "dye_shulkers");
        // Save Interval
        IntegerSetting save_interval = new IntegerSetting(6000, "save_interval").onChanged(integer -> patch_save_interval = integer);
        // Tnt duping
        BooleanSetting tnt_duping = new BooleanSetting(true, "tnt_duping").onChanged(b -> patch_tnt_duping = b);
        // Eigencraft redstone
        BooleanSetting eigencraft_redstone = new BooleanSetting(false, "eigencraft_redstone").onChanged(b -> patch_eigencraft_redstone = b);
        // Stuck Wither
        CategorySetting wither = new CategorySetting("wither");
        IntegerSetting check_distance = new IntegerSetting(2, "check_distance").range(-256, 256).onChanged(integer -> wither_check_distance = integer);
        DoubleSetting tp_distance = new DoubleSetting(0D, "tp_distance").range(-256D, 256D).onChanged(d -> wither_tp_distance = d);
        wither.addChild(check_distance);
        wither.addChild(tp_distance);

        // Entity merging
        CategorySetting item_merge = new CategorySetting("item_merge");
        BooleanSetting adjust_movement = new BooleanSetting(true, "adjust_movement");
        item_merge.addChild(adjust_movement);

        // Shulker spawn chance
        DoubleSetting shulker_spawn_chance = new DoubleSetting(0D, "shulker_spawn_chance");

        // Global sound
        BooleanSetting global_sound = new BooleanSetting(true, "global_sound");

        patch.addChild(dye_shulkers);
        patch.addChild(save_interval);
        patch.addChild(tnt_duping);
        patch.addChild(eigencraft_redstone);
        patch.addChild(wither);
        patch.addChild(item_merge);
        patch.addChild(shulker_spawn_chance);
        patch.addChild(global_sound);

        // View distance
        IntegerSetting view_distance = new IntegerSetting(10, "view_distance").onChanged(distance -> KiloEssentials.getMinecraftServer().getPlayerManager().setViewDistance(distance));

        // Entity Limit
        CategorySetting entity_limit = new CategorySetting("entity_limit");
        List<String> limit_entries = new ArrayList<>();
        for (EntityType<?> entityType : new EntityType[]{EntityType.GUARDIAN, EntityType.ITEM_FRAME, EntityType.CHICKEN}) {
            limit_entries.add(Registry.ENTITY_TYPE.getId(entityType).getPath());
        }
        for (String limit_entry : limit_entries) {
            CategorySetting entity = new CategorySetting(limit_entry);
            IntegerSetting limit = new IntegerSetting(-1, "limit");
            IntegerSetting range = new IntegerSetting(-1, "range");
            entity.addChild(limit);
            entity.addChild(range);
            entity_limit.addChild(entity);
        }

        // Mobcap
        ServerSettings.mobcap = new float[RegistryUtils.getWorldsKeySet().size()][SpawnGroup.values().length + 1];
        CategorySetting mobcap = new CategorySetting("mobcap");
        int worldID = 0;
        for (RegistryKey<World> registryKey : RegistryUtils.getWorldsKeySet()) {
            ((RegistryKeyID) registryKey).setID(worldID);
            FloatSetting world = new FloatSetting(1F, registryKey.getValue().getPath()).range(0F, 100F).onChanged(f -> ServerSettings.mobcap[((RegistryKeyID) registryKey).getID()][0] = f);
            for (SpawnGroup spawnGroup : SpawnGroup.values()) {
                FloatSetting group = new FloatSetting(1F, spawnGroup.getName().toLowerCase()).range(0F, 100F).onChanged(f -> ServerSettings.mobcap[((RegistryKeyID) registryKey).getID()][spawnGroup.ordinal() + 1] = f);
                world.addChild(group);
            }
            worldID++;
            mobcap.addChild(world);
        }

        root.addChild(debug);
        root.addChild(entity_limit);
        root.addChild(mobcap);
        root.addChild(patch);
        root.addChild(view_distance);
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("settings.dat", KiloEssentials.getDataDirPath());
    }

    @Override
    public NbtCompound serialize() {
        return root.toTag();
    }

    @Override
    public void deserialize(@NotNull NbtCompound tag) {
        root.fromTag(tag);
    }

}
