package org.kilocraft.essentials.util.nbt;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtIo;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NBTStorageUtil {
    private static List<NBTStorage> callbacks = new ArrayList<>();

    public static <C extends NBTStorage> void addCallback(C callback) {
        callbacks.add(callback);
    }

    public static void onSave() {
        try {
            save();
        } catch (IOException e) {
            KiloEssentials.getLogger().fatal("Cannot save the data files!", e);
        }
    }

    public static void onLoad() {
        try {
            load();
        } catch (IOException e) {
            KiloEssentials.getLogger().fatal("Cannot load the data files! ", e);
        }
    }

    private static void load() throws IOException {
        for (NBTStorage callback : callbacks) {
            if (SharedConstants.isDevelopment) {
                KiloDebugUtils.getLogger().info("Loading data file {} : \"{}\"", callback.getSaveFile().getFile().getName(), callback.getSaveFile().getAbsolutePath());
            }

            if (!callback.getSaveFile().exists()) {
                save();
                continue;
            }

            callback.deserialize(NbtIo.readCompressed(new FileInputStream(callback.getSaveFile().getFile())));
        }
    }

    private static void save() throws IOException {
        for (NBTStorage callback : callbacks) {
            if (SharedConstants.isDevelopment) {
                KiloDebugUtils.getLogger().info("Saving data file {} : \"{}\"", callback.getSaveFile().getFile().getName(), callback.getSaveFile().getAbsolutePath());
            }

            save(callback);
        }
    }

    private static void save(NBTStorage nbtStorage) throws IOException {
        if (!nbtStorage.getSaveFile().exists()) {
            nbtStorage.getSaveFile().createFile();
        }

        NbtIo.writeCompressed(nbtStorage.serialize(), new FileOutputStream(nbtStorage.getSaveFile().getFile()));
    }

}
