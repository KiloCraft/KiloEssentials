package org.kilocraft.essentials.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.config_old.KiloConfigOLD;

public class KiloConfig {

    public KiloConfig() {
        FileConfig config = FileConfig.of(KiloConfigOLD.getConfigPath() + "essentials.yml");
        config.load();
        config.set("the.long.path.in.the.config", "ThaValue");
        config.set("thisIs.a.test", new String[]{"OwO", "UwU"});
        config.set("thisIs.a.test", 12);
        config.save();

        ConfigData obj = new ObjectConverter().toObject(config, ConfigData::new);
        System.out.println("Obj: " + obj);

        System.out.println(obj.server_name);

        Config confingFromObject = new ObjectConverter().toConfig(obj, Config::inMemory);

        System.out.println("cfg: " + confingFromObject);
    }

}
