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
import org.kilocraft.essentials.patch.entityActivationRange.ActivationRange;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;
import org.kilocraft.essentials.util.registry.RegistryKeyID;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.settings.values.*;
import org.kilocraft.essentials.util.settings.values.util.RootSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.kilocraft.essentials.patch.entityActivationRange.ActivationRange.activationRange;

public class ServerSettings implements NBTStorage {

    //TODO: Figure out a clever way to cache values
    //if we ever get more than 256 entities this will throw IndexOutOfBoundsException
    private static final int entities = 128;
    public static final boolean[] entityTickCache = new boolean[entities];
    public static final boolean[] entitySpawnCache = new boolean[entities];
    public static RootSetting root = new RootSetting();
    public static boolean perPlayerMobcap = false;
    public static int wither_check_distance = 2;
    public static double wither_tp_distance = 1;
    public static float[][] mobcap;
    public static boolean tickInactiveVillagers = false;
    public static int villagerWorkImmunityAfter = 5 * 20;
    public static int villagerWorkImmunityFor = 20;
    public static boolean villagerActiveForPanic = true;
    public static boolean patch_item_merge_adjust_movement = true;
    public static double patch_item_merge_radius = 0.5D;
    public static boolean patch_lobotomize_villagers_enabled = false;
    public static int patch_lobotomize_villagers_tick_interval = 20;
    public static boolean tick_utils_automated = false;
    public static int tick_utils_tick_distance = getViewDistance();
    public static int tick_utils_min_view_distance = 2;
    public static int tick_utils_max_view_distance = getViewDistance();
    public static int tick_utils_min_tick_distance = tick_utils_min_view_distance;
    public static int tick_utils_max_tick_distance = tick_utils_max_view_distance;
    public static float tick_utils_min_mobcap = 0F;
    public static float tick_utils_max_mobcap = 1F;
    public static float tick_utils_global_mobcap = 1F;


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
        //Custom settings
        IntegerSetting viewDistance = new IntegerSetting(10, "view_distance").onChanged(distance -> KiloEssentials.getMinecraftServer().getPlayerManager().setViewDistance(distance));
        BooleanSetting debug = new BooleanSetting(false, "debug").onChanged(b -> SharedConstants.isDevelopment = b);

        //Patches
        CategorySetting patch = new CategorySetting("patch");
        //Donkey dupe
        BooleanSetting donkeyDupe = new BooleanSetting(true, "donkey_dupe");
        //Donkey dupe
        BooleanSetting loadSpawn = new BooleanSetting(true, "load_spawn");
        //Stuck Wither
        CategorySetting wither = new CategorySetting("wither");
        IntegerSetting check_distance = new IntegerSetting(2, "check_distance").range(-256, 256).onChanged(integer -> wither_check_distance = integer);
        DoubleSetting tp_distance = new DoubleSetting(0D, "tp_distance").range(-256D, 256D).onChanged(d -> wither_tp_distance = d);
        wither.addChild(check_distance);
        wither.addChild(tp_distance);
        //per-player-mobcap
        BooleanSetting ppmobcap = new BooleanSetting(false, "ppmobcap").onChanged(bool -> perPlayerMobcap = bool);

        //Entity merging
        CategorySetting item_merge = new CategorySetting("item_merge");
        DoubleSetting radius = new DoubleSetting(0.5D, "radius").range(0D, 256D).onChanged(d -> patch_item_merge_radius = d);
        BooleanSetting adjust_movement = new BooleanSetting(true, "adjust_movement");
        item_merge.addChild(radius);
        item_merge.addChild(adjust_movement);
        //Shulker spawn chance
        DoubleSetting shulker_spawn_chance = new DoubleSetting(0D, "shulker_spawn_chance");

        //Lobotomize Stuck Villagers
        CategorySetting lobotomize_villagers = new CategorySetting("lobotomize_villagers");
        {
            BooleanSetting enabled = new BooleanSetting(false, "enabled").onChanged(bool -> patch_lobotomize_villagers_enabled = bool);
            IntegerSetting tick_interval = new IntegerSetting(20, "tick_interval").onChanged(integer -> patch_lobotomize_villagers_tick_interval = integer);
            lobotomize_villagers.addChild(enabled);
            lobotomize_villagers.addChild(tick_interval);
        }

        //Global sound
        BooleanSetting global_sound = new BooleanSetting(true, "global_sound");

        patch.addChild(donkeyDupe);
        patch.addChild(loadSpawn);
        patch.addChild(wither);
        patch.addChild(ppmobcap);
        patch.addChild(item_merge);
        patch.addChild(shulker_spawn_chance);
        patch.addChild(lobotomize_villagers);
        patch.addChild(global_sound);

        //Activation range
        CategorySetting activation_range = new CategorySetting("activation_range");
        CategorySetting general = new CategorySetting("general");
        for (ActivationRange.ActivationType activationType : ActivationRange.ActivationType.values()) {
            CategorySetting type = new CategorySetting(activationType.toString().toLowerCase());
            IntegerSetting range = new IntegerSetting(activationType.getActivationRange(), "range").onChanged(integer -> activationRange[activationType.ordinal()][0] = integer);
            IntegerSetting maxPerTick = new IntegerSetting(activationType.getWakeUpInactiveMaxPerTick(), "max_per_tick").onChanged(integer -> activationRange[activationType.ordinal()][1] = integer);
            IntegerSetting checkEvery = new IntegerSetting(activationType.getWakeUpInactiveEvery(), "check_every").onChanged(integer -> activationRange[activationType.ordinal()][2] = integer);
            IntegerSetting wakeupFor = new IntegerSetting(activationType.getWakeUpInactiveFor(), "wakeup_for").onChanged(integer -> activationRange[activationType.ordinal()][3] = integer);
            type.addChild(range).addChild(maxPerTick).addChild(checkEvery).addChild(wakeupFor);
            general.addChild(type);
        }
        activation_range.addChild(general);
        {
            CategorySetting custom = new CategorySetting("custom");
            CategorySetting villager = new CategorySetting("villager");
            BooleanSetting tickInactive = new BooleanSetting(true, "tick_inactive").onChanged(bool -> tickInactiveVillagers = bool);
            IntegerSetting workImmunityAfter = new IntegerSetting(5 * 20, "work_immunity_after").onChanged(integer -> villagerWorkImmunityAfter = integer);
            IntegerSetting workImmunityFor = new IntegerSetting(5 * 20, "work_immunity_for").onChanged(integer -> villagerWorkImmunityFor = integer);
            BooleanSetting activeForPanice = new BooleanSetting(true, "active_for_panic").onChanged(bool -> villagerActiveForPanic = bool);
            villager.addChild(tickInactive).addChild(workImmunityAfter).addChild(workImmunityFor).addChild(activeForPanice);

            custom.addChild(villager);
            activation_range.addChild(custom);
        }

        //Tick distance
        CategorySetting tick_utils = new CategorySetting("tick_utils");
        CategorySetting automated = new CategorySetting("automated");
        BooleanSetting enabled = new BooleanSetting(false, "enabled").onChanged(bool -> tick_utils_automated = bool);
        IntegerSetting tick_distance = new IntegerSetting(-1, "tick_distance").onChanged(integer -> tick_utils_tick_distance = integer);
        IntegerSetting min_tick_distance = new IntegerSetting(2, "min_tick_distance").onChanged(integer -> tick_utils_min_tick_distance = integer);
        IntegerSetting max_tick_distance = new IntegerSetting(getViewDistance(), "max_tick_distance").onChanged(integer -> tick_utils_max_tick_distance = integer);
        IntegerSetting min_view_distance = new IntegerSetting(2, "min_view_distance").onChanged(integer -> tick_utils_min_view_distance = integer);
        IntegerSetting max_view_distance = new IntegerSetting(getViewDistance(), "max_view_distance").onChanged(integer -> tick_utils_max_view_distance = integer);
        FloatSetting min_mobcap = new FloatSetting(0F, "min_mobcap").onChanged(d -> tick_utils_min_mobcap = d);
        FloatSetting max_mobcap = new FloatSetting(1F, "max_mobcap").onChanged(d -> tick_utils_max_mobcap = d);
        FloatSetting global_mobcap = new FloatSetting(1F, "global_mobcap").onChanged(d -> tick_utils_global_mobcap = d);

        automated.addChild(enabled);
        automated.addChild(min_tick_distance);
        automated.addChild(max_tick_distance);
        automated.addChild(min_view_distance);
        automated.addChild(max_view_distance);
        automated.addChild(min_mobcap);
        automated.addChild(max_mobcap);

        Arrays.fill(entityTickCache, true);
        BooleanSetting entityTicking = new BooleanSetting(true, "entity").onChanged(bool -> entityTickCache[0] = bool);
        for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
            BooleanSetting value = new BooleanSetting(true, Registry.ENTITY_TYPE.getId(entityType).getPath()).onChanged(bool -> entityTickCache[Registry.ENTITY_TYPE.getRawId(entityType) + 1] = bool);
            entityTicking.addChild(value);
        }

        tick_utils.addChild(automated);
        tick_utils.addChild(entityTicking);
        tick_utils.addChild(tick_distance);
        tick_utils.addChild(global_mobcap);

        //Entity Limit
        CategorySetting entity_limit = new CategorySetting("entity_limit");
        List<String> limit_entries = new ArrayList<>();
        for (EntityType entityType : new EntityType[]{EntityType.GUARDIAN, EntityType.ITEM_FRAME, EntityType.CHICKEN, EntityType.VILLAGER}) {
            limit_entries.add(Registry.ENTITY_TYPE.getId(entityType).getPath());
        }
        limit_entries.add("animals");
        for (String limit_entry : limit_entries) {
            CategorySetting entity = new CategorySetting(limit_entry);
            IntegerSetting limit = new IntegerSetting(-1, "limit");
            IntegerSetting range = new IntegerSetting(-1, "range");
            entity.addChild(limit);
            entity.addChild(range);
            entity_limit.addChild(entity);
        }

        //Spawning
        CategorySetting spawn = new CategorySetting("spawn");
        Arrays.fill(entitySpawnCache, true);
        BooleanSetting spawnEntity = new BooleanSetting(true, "entity").onChanged(bool -> entitySpawnCache[0] = bool);
        for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
            BooleanSetting value = new BooleanSetting(true, Registry.ENTITY_TYPE.getId(entityType).getPath()).onChanged(bool -> entitySpawnCache[Registry.ENTITY_TYPE.getRawId(entityType) + 1] = bool);
            spawnEntity.addChild(value);
        }

        spawn.addChild(spawnEntity);

        //Mobcap
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

        root.addChild(activation_range);
        root.addChild(viewDistance);
        root.addChild(debug);
        root.addChild(tick_utils);
        root.addChild(entity_limit);
        root.addChild(spawn);
        root.addChild(mobcap);
        root.addChild(patch);
        KiloCommands.register(new SettingCommand());
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
