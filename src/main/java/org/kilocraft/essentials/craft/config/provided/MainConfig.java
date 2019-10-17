package org.kilocraft.essentials.craft.config.provided;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.config.KiloConifg;

import java.util.ArrayList;
import java.util.List;

public class MainConfig implements ConfigProvided {

    @Override
    public List<String> configValues() {
        return new ArrayList<String>(){{
            add("server$name");
            add("server$description");
            add("server$playercountmessage");
            add("warps$permission_prefix");
        }};
    }

    public String server$name = "";
    public String server$description = "";
    public String server$playercountmessage = "";
    public String warps$permission_prefix = "";

    @Override
    public FileConfig config() {
        return KiloConifg.getFileConfigOfMain();
    }

    @Override
    public <T> T get(String key) {
        return config().get(key);
    }

}
