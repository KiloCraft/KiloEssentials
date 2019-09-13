package org.kilocraft.essentials.craft;

import com.mojang.brigadier.CommandDispatcher;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TestCommand;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.commands.KiloAPICommands;
import org.kilocraft.essentials.api.util.SomeGlobals;
import org.kilocraft.essentials.craft.commands.VersionCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ReloadCommand;

public class KiloCommands {
    private static CommandDispatcher<ServerCommandSource> dispatcher;
    public KiloCommands(boolean dev) {
        dispatcher = SomeGlobals.commandDispatcher;
        // Thimble.registerDispatcherCommands(dispatcher); uncomment when we have a custom dispatcher
        register(dev);
    }

    private static void register(boolean devEnv) {
        if (devEnv) {
            Mod.getLogger().info("Registering developer commands...");
            KiloAPICommands.register(dispatcher);
            TestCommand.register(dispatcher);
        }

        VersionCommand.register(dispatcher);
        ReloadCommand.register(dispatcher);



    }

}
