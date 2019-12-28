package org.kilocraft.essentials.util;

import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.config.ConfigValueGetter;
import org.kilocraft.essentials.config.KiloConfig;

import java.io.*;

public class StartupScript {
    private File resourceFile;
    private File file;
    private String FILE_NAME;
    private String MAX_MEMORY;
    private String STARTUP_CODE;

    public StartupScript() {
        ConfigValueGetter config = KiloConfig.getProvider().getMain();
        this.FILE_NAME = config.getStringSafely("startup-script.script-name", "start") + ".sh";
        this.file = new File(System.getProperty("user.dir") + File.separator + FILE_NAME);

        if (file.exists())
            return;

        KiloEssentials.getLogger().info("Generating the start script...");
        this.MAX_MEMORY = config.getStringSafely("startup-script.maximum-memory-size", "2G");
        String SCREEN_NAME = config.getStringSafely("startup-script.linux-screen-name", "mc-server");
        String LOADER_NAME = config.getStringSafely("startup-script.fabric-loader-name", "fabric-server-launch.jar");
        boolean generateForLinuxScreen = config.getBooleanSafely("startup-script.linux-screen-mode", false);

        this.resourceFile = new File(
                Thread.currentThread().getContextClassLoader().getResource("assets/start-script.sh").getFile());

        String normalScript = "java -jar -Xmx" + this.MAX_MEMORY + " " + LOADER_NAME;
        String screenScript = "screen -S " + SCREEN_NAME + " " + normalScript;
        this.STARTUP_CODE = generateForLinuxScreen ? screenScript : normalScript;

        System.out.println(this.FILE_NAME + " " + this.MAX_MEMORY + " " + this.STARTUP_CODE);

        generate();
    }

    private void generate() {
        FileWriter writer = null;

        try {
            writer = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String oldContent = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(resourceFile));
            String line = reader.readLine();

            assert writer != null;
            while (line != null) {
                oldContent = line + System.lineSeparator();
                String newContent = oldContent.replace("%MAXIMUM_RAM%", this.MAX_MEMORY)
                        .replace("%STARTUP_CODE%", this.STARTUP_CODE)
                        .replace("%SERVER_NAME%", KiloServer.getServer().getName());

                writer.write(newContent);
                line = reader.readLine();
            }

            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            KiloEssentials.getLogger().info("Generated the Start script \"" + this.FILE_NAME + "\"");
        }
    }

}
