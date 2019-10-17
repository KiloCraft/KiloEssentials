package org.kilocraft.essentials.craft.config.provided;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.config.KiloConifg;

import java.util.ArrayList;
import java.util.List;

public class MessagesConfig implements ConfigProvided {

    @Override
    public List<String> configValues() {
        return new ArrayList<String>(){{}};
    }

    @Override
    public FileConfig config() {
        return KiloConifg.getFileConfigOfMessages();
    }

    @Override
    public <T> T get(String key) {
        return config().get(key);
    }
}
