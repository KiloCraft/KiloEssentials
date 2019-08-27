package org.kilocraft.essentials.config;

import org.kilocraft.essentials.KiloEssentials;
import org.kilocraft.essentials.Mod;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConfigFileHelper {
    private static String ConfigDirPath = "^KiloEssentials^Config".replace("^", File.separator);
    private static String ResourceConfigDirectoryPath = "ConfigFiles" + File.separator;
    private static String WorkingDirPath = System.getProperty("user.dir");

    private static File ConfigDirFile = new File(WorkingDirPath + ConfigDirPath);

    public static void loadConifgFiles(String cfgName) {
        File cfg = new File(ConfigDirFile.getAbsolutePath() + File.separator +  cfgName);
        System.out.println(ConfigDirFile.getAbsolutePath() + File.separator +  cfgName);

        try (InputStream inputStream = new FileInputStream(cfg)) {
            if (cfg.exists()) {
                KiloEssentials.getLogger.info(Mod.messages.getProperty("cfghandler.load.successfull"), cfgName);
                ConfigLoader.load(cfg);
            } else generateConfigFile(cfg);

        } catch (FileNotFoundException e) {
            KiloEssentials.getLogger.info(Mod.messages.getProperty("cfghandler.generate"), cfgName);
        } catch (IOException e) {
            KiloEssentials.getLogger.info(Mod.messages.getProperty("cfghandler.generate.error"), cfgName, e.getCause());
        }

    }

    private static void generateConfigFile(File cfg) {
        if (!ConfigDirFile.exists()) ConfigDirFile.mkdirs();

        try {
            cfg.createNewFile();
        } catch (IOException e) {
            KiloEssentials.getLogger.info(Mod.messages.getProperty("cfghandler.generate.error"), cfg.getName(), e.getCause());
        } finally {
            copyConfigData(cfg);
            if (cfg.exists()) loadConifgFiles(cfg.getName());
        }

    }


    private static void copyConfigData(File cfg) {
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ResourceConfigDirectoryPath + cfg.getName());
            Files.copy(inputStream, Paths.get(cfg.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);

        } catch (FileNotFoundException e) {
            KiloEssentials.getLogger.error(Mod.messages.getProperty("cfghandler.generate.copy.failed"));
        } catch (IOException e) {
            e.printStackTrace();
            KiloEssentials.getLogger.error("An unexpected error occured during getting the config file \"{}\"\n Caused by: \"{}\"\n" +
                    "Restarting the server might help you to resolve this issue.", cfg.getName(), e.getCause());
        }


    }
}
