package org.kilocraft.essentials.craft.threaded;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.provider.LocateStructureProvider;

public class ThreadedStructureLocator implements KiloThread, Runnable {
    private ServerCommandSource source;
    private String name;
    public ThreadedStructureLocator(ServerCommandSource commandSource, String structureName) {
        source = commandSource;
        name = structureName;
        KiloEssentials.getLogger().info("Started thread StructureLocator by %s for structure \"%s\"", source.getName(), name);
    }

    @Override
    public String getName() {
        return "StructureLocator";
    }

    @Override
    public void run() {
        try {
            LocateStructureProvider.execute(source, name);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Logger getLogger() {
        return LogManager.getFormatterLogger();
    }

}
