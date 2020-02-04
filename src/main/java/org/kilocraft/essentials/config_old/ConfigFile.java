package org.kilocraft.essentials.config_old;

import org.kilocraft.essentials.api.ModConstants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConfigFile {
    private File config, configDir;
    private String configRes;
    boolean dontGenerate;
    @Deprecated
    public static final String currentDir = System.getProperty("user.dir");

    @Deprecated()
    public ConfigFile(String configFileName, String configFileDirectory, String resourceBaseFile, boolean dontGenerateCfg, boolean logToConsole) {
        this.configDir = new File(currentDir + configFileDirectory);
        this.config = new File(configDir.getAbsolutePath() + File.separator + configFileName);
        this.configRes = resourceBaseFile + File.separator + config.getName();
        this.dontGenerate = dontGenerateCfg;

        load(config, configDir, configRes, dontGenerate, logToConsole);
    }

    private static void load(File configFile, File configDirectory, String fileToCopyFrom, boolean dontGenerate, boolean log) {

        try (InputStream inputStream = new FileInputStream(configFile)){
            if (configFile.exists() && log) ModConstants.getLogger().info(ModConstants.getLang().getProperty("cfghandler.load.successfull"), configFile.getName());

        } catch (FileNotFoundException e) {
            ModConstants.getLogger().warn(ModConstants.getLang().getProperty("cfghandler.generate.start"), configFile.getName());
            if (!dontGenerate) generate(configFile, configDirectory, fileToCopyFrom);
        } catch (IOException e) {
            ModConstants.getLogger().error(ModConstants.getLang().getProperty("cfghandler.generate.error"), configFile.getName(), e.getCause());
            e.printStackTrace();
        }
    }

    private static void generate(File cfg, File cfgDir, String fileToCopyFrom) {
        if (!cfgDir.exists()) cfgDir.mkdirs();
        try {
            cfg.createNewFile();
        } catch (IOException e) {
            ModConstants.getLogger().error(ModConstants.getLang().getProperty("cfghandler.generate.error"), cfg.getName(), e.getCause());
        } finally {
            if (cfg.exists()) {
                ModConstants.getLogger().info(ModConstants.getLang().getProperty("cfghandler.generate.successfull"), cfg.getName());
                if (fileToCopyFrom != null) copyConfigData(cfg, fileToCopyFrom);
            }
        }
    }

    private static void copyConfigData(File cfg, String fileToCopyFrom) {
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileToCopyFrom);
            Files.copy(inputStream, Paths.get(cfg.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | NullPointerException e) {
            ModConstants.getLogger().error(ModConstants.getLang().getProperty("cfghandler.generate.copy.failed"));
            ModConstants.getLogger().error("An unexpected error occured during getting the config file \"%s\"\n Caused by: \"%s\"\n" +
                    "Restarting the server might help you to resolve this issue.", cfg.getName(), e.getCause());
        }
    }

}
