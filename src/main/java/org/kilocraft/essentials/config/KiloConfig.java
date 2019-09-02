package org.kilocraft.essentials.config;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.files.FilesConfigurationSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class KiloConfig {
    private static ConfigurationSource generalConfigSource;
    protected static ConfigurationProvider generalConfigProvider;
    private static ConfigurationSource messagesConfigSource;
    protected static ConfigurationProvider messagesConfigProvider;
    private static ConfigurationSource ranksConfigSource;
    protected static ConfigurationProvider ranksConfigProvider;

    private File config;
    private InputStream inputStream;
    public KiloConfig(String pathName, String fileName) throws FileNotFoundException {
        this.config = new File(ConfigFile.currentDir + pathName + fileName);
        this.inputStream = new FileInputStream(config);
        switch (fileName) {
            case "General.yml":
                generalConfig(config);
            case "Messages.yml":

            case "Ranks.yml":

            break;
        }
    }

    private static void generalConfig(File file) {
        generalConfigSource = new FilesConfigurationSource();
    }
}
