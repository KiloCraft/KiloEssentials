package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.chat.LangText;

import java.util.ArrayList;
import java.util.List;

public class VersionCommand {
	
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        List<String> list = new ArrayList<String>(){{
            add("version");
            add("kiloessentials");
            add("mod");
        }};

        list.forEach((name) -> dispatcher.register(
                CommandManager.literal(name).executes(context -> {
                    LangText.sendToUniversalSource(context.getSource(),
                            "commands.version.info",
                            false,
                            Mod.getMinecraftVersion(),
                            Mod.getLoaderVersion(),
                            Mod.getMappingsVersion(),
                            Mod.getVersion());
                    return 1;
                })
        ));

    }
    
}
