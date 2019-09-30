package org.kilocraft.essentials.craft.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.KiloEssentials;

import java.util.HashMap;

public class KiloConifg {
    private String workingDir = System.getProperty("user.dir");
    private String configPath = workingDir + "/KiloEssentials/config/";
    private String resourcePath = "assets/configurations/";
    private HashMap<String, String> configFiles = new HashMap<String, String>(){{
        put("KiloEssentials.yaml", workingDir + "/");
        put("Messages.yaml", configPath);
    }};

    private FileConfig config;
    private FileConfig messages;

    public KiloConifg() {
        handle();
        KiloEssentials.getLogger().info("Config files are loaded successfully");
    }

    private void handle() {
        try {
            configFiles.forEach((name, path) -> {
                KiloFile file = new KiloFile(name, path);
                file.tryToLoad(resourcePath + name);
                defineConfig(file);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void defineConfig(KiloFile kiloFile) {
        switch (kiloFile.getFile().getName()) {
            case "KiloEssentials.yaml":
                config = FileConfig.of(kiloFile.getFile());
            case "Messages.yaml":
                messages = FileConfig.of(kiloFile.getFile().getName());
        }
    }

    public FileConfig getMain() {
        return config;
    }

    public FileConfig getMessages() {
        return messages;
    }

}