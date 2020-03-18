package org.kilocraft.essentials.extensions.warps.playerwarps;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.feature.ReloadableConfigurableFeature;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.warps.playerwarps.commands.PlayerWarpCommand;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.NBTStorageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PlayerWarpsManager implements ReloadableConfigurableFeature, NBTStorage {
    private static boolean enabled = false;
    private static ArrayList<String> byName = new ArrayList<>();
    private static List<PlayerWarp> warps = new ArrayList<>();

    @Override
    public boolean register() {
        enabled = true;
        NBTStorageUtil.addCallback(this);

        List<EssentialCommand> commands = new ArrayList<EssentialCommand>(){{
            this.add(new PlayerWarpCommand("playerwarp", CommandPermission.PLAYER_WARP, new String[]{"pwarp"}));
        }};

        for (EssentialCommand command : commands) {
            KiloEssentials.getInstance().getCommandHandler().register(command);
        }

        load();
        return true;
    }

    @Override
    public void load() {
        for (String type : KiloConfig.main().playerWarpTypes) {
            PlayerWarp.Type.add(type);
        }
    }

    public static List<PlayerWarp> getWarps() { // TODO Move all access to Feature Types in future.
        return warps;
    }

    public static ArrayList<String> getWarpsByName() {
        return byName;
    }

    public static void addWarp(PlayerWarp warp) {
        warps.add(warp);
        byName.add(warp.getName());
    }

    public static void removeWarp(PlayerWarp warp) {
        warps.remove(warp);
        byName.remove(warp.getName());
    }

    public static void removeWarp(String name) {
        PlayerWarp warp = getWarp(name);

        if (warp != null) {
            removeWarp(warp);
        }
    }

    @Nullable
    public static PlayerWarp getWarp(String warp) {
        for (PlayerWarp w : warps) {
            if (w.getName().toLowerCase(Locale.ROOT).equals(warp.toLowerCase(Locale.ROOT))) {
                return w;
            }
        }

        return null;
    }

    public static List<PlayerWarp> getWarps(UUID owner) {
        final List<PlayerWarp> list = new ArrayList<>();
        for (PlayerWarp warp : warps) {
            if (warp.getOwner().equals(owner)) {
                list.add(warp);
            }
        }

        return list;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("player_warps.dat", KiloEssentials.getDataDirPath());
    }


    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        for (PlayerWarp warp : warps) {
            tag.put(warp.getName(), warp.toTag());
        }

        return tag;
    }

    @Override
    public void deserialize(@NotNull CompoundTag compoundTag) {
        warps.clear();
        byName.clear();
        compoundTag.getKeys().forEach((key) -> {
            warps.add(new PlayerWarp(key, compoundTag.getCompound(key)));
            byName.add(key);
        });
    }
}
