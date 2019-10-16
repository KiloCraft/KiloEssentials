package org.kilocraft.essentials.craft.config.provided;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.config.KiloConifg;

public class MainConfig {
    public MainConfig() {
        provide();
    }

    /**
     * Server
     */
    private char altColorChar;
    private String name;
    private String motd;
    private String playerCountMessage;

    private FileConfig config = KiloConifg.getMain();

    private void provide() {
        name = config.getOrElse("server.name", "Minecraft server");
        motd = config.getOrElse("server.motd", "NULL");
        playerCountMessage = config.getOrElse("server.playercountmessage", "NULL");


    }

}
