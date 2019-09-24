package org.kilocraft.essentials.api.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class NbtFile {
    /**
     * @Author CODY_AI
     */

    private String name;
    private File fileDir;
    private File file;
    private CompoundTag compoundTag;

    public NbtFile(String path, String name, boolean oldDat) {
        String type = ".dat";
        if (oldDat) type = ".dat_old";
        this.name = name;
        this.fileDir = new File(ConfigFile.currentDir + path);
        this.file = new File(ConfigFile.currentDir + path + name + ".dat");
    }


    public void load() {
        if (!this.file.exists()) generate();
        else {
            try {
                this.compoundTag = NbtIo.readCompressed(new FileInputStream(this.file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generate() {
        this.fileDir.mkdirs();
        try {
            this.file.createNewFile();
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        load();
    }

    public void save() {
        try {
            NbtIo.writeCompressed(this.compoundTag, new FileOutputStream(this.file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return this.file;
    }

    public CompoundTag getCompoundTag() {
        return this.compoundTag;
    }
}
