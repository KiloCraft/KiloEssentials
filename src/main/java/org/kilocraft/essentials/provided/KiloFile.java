package org.kilocraft.essentials.provided;

import org.kilocraft.essentials.api.KiloEssentials;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class KiloFile {
    private File file;
    private File dir;
    private String name;

    public KiloFile(String name, String path) {
        this.name = name;
        this.dir = new File(path);
        this.file = new File(name);
    }

    public KiloFile(String name, Path path) {
        this.name = name;
        this.dir = path.toFile();
        this.file = path.resolve(name).toFile();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void createFile() {
        if (this.file.exists())
            return;

        try {
            this.dir.mkdirs();
            this.file.createNewFile();
        } catch (IOException e) {
            KiloEssentials.getLogger().error("Exception while creating the file " + this.file.getName() + ": " + this.file.getPath());
            e.printStackTrace();
        }
    }

    public boolean exists() {
        return this.file.exists();
    }

    public File getFile() {
        return this.file;
    }

    public String getAbsolutePath() {
        return this.file.getAbsolutePath();
    }

    public void pasteFromResources(String resourcesPath) {
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcesPath);
            Files.copy(inputStream, Paths.get(this.file.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}