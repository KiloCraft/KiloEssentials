package org.kilocraft.essentials.craft.config.provided;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.config.KiloConifg;

import java.util.ArrayList;
import java.util.List;

public class MainConfig implements ConfigProvided {

    @Override
    public List<String> configValues() {
        return new ArrayList<String>(){{
            add("server_name");
            add("server_description");
            add("server_playercountmessage");
        }};
    }

    public String server_name = "";
    public String server_description = "";
    public String server_playercountmessage = "";

    @Override
    public FileConfig config() {
        return KiloConifg.getMain();
    }

    @Override
    public <T> T get(String key) {
        return config().get(key);
    }

}
