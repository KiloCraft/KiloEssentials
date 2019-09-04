package org.kilocraft.essentials.api.Config;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.source.ConfigurationSource;

import java.io.File;
import java.util.ArrayList;

public class ConfigLoader {
    private ConfigurationSource configurationSource;
    private ConfigurationProvider configurationProvider;
    private ArrayList<File> configFiles;
    public ConfigLoader(ArrayList<File> configFiles) {
        this.configFiles = configFiles;

    }

}
