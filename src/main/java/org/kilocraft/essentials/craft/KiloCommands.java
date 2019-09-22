package org.kilocraft.essentials.craft;


import com.mojang.brigadier.CommandDispatcher;
import io.github.indicode.fabric.permissions.Thimble;
import io.github.indicode.fabric.permissions.command.CommandPermission;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TestCommand;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.util.SomeGlobals;
import org.kilocraft.essentials.craft.commands.VersionCommand;
import org.kilocraft.essentials.craft.commands.essentials.*;
import org.kilocraft.essentials.craft.commands.essentials.ItemCommands.ItemCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ReloadCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ServerModNameCommand;

import java.lang.reflect.InvocationTargetException;

public class KiloCommands {
    private static CommandDispatcher<ServerCommandSource> dispatcher;
    public KiloCommands(boolean dev) {
        dispatcher = SomeGlobals.commandDispatcher;
        /*Thimble.permissionWriters.add(pair -> { // How to "register" permissions
            Thimble.PERMISSIONS.getPermission("xxx"); // Normal JSON permission. Saves to file & dosen't update command tree
            try {
                Thimble.PERMISSIONS.getPermission("yyy", CommandPermission.class); // Permission that updates command tree, and dosen't save to file
                // Syntax is getPermission(name, default class); Note that default class must have the constructor (String name, Permission parent)
            } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });*/
        // Thimble.registerDispatcherCommands(dispatcher); uncomment when we have a custom dispatcher
        register(dev);
    }

    private static void register(boolean devEnv) {
        if (devEnv) {
            Mod.getLogger().info("Registering developer commands...");
            TestCommand.register(dispatcher);
        }

        VersionCommand.register(dispatcher);
        ReloadCommand.register(dispatcher);
        EnderchestCommand.register(dispatcher);
        TpaCommand.register(dispatcher);
        //ServerCommand.register(dispatcher);
        ItemCommand.register(dispatcher);
        AnvilCommand.register(dispatcher);
        CraftingbenchCommand.register(dispatcher);
        LocateBiomeCommand.register(dispatcher);
        NickCommand.register(dispatcher);
        ServerModNameCommand.register(dispatcher);

    }

}
