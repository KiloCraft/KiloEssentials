package org.kilocraft.essentials.provided;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class KiloFile {
    private File dir;
    private File file;
    private String name, path;

    public KiloFile(String name, String path) {
        this.path = path;
        this.dir = new File(path);
        this.file = new File(path + name);
    }

    public KiloFile(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public KiloFile(String name, Path path) {
        this.name = name;
        this.file = new File( path.toFile().getAbsolutePath());
    }


    public boolean tryToLoad() {
        if (this.file.exists()) return true;
        else return createFile();
    }

    public boolean tryToLoad(String resourcesFilePath) {
        if (this.file.exists()) return true;
        else if (createFile()) pasteFromResources(resourcesFilePath);

        return this.file.exists();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean createFile() {
        if (!this.file.exists()) {
            try {
                this.dir.mkdirs();
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return this.file.exists();
    }

    public boolean exists() {
        return this.file.exists();
    }

    public File getFile() {
        return this.file;
    }

    public void pasteFromResources(String resourcesPath) {
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcesPath);
            Files.copy(inputStream, Paths.get(this.file.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }

}