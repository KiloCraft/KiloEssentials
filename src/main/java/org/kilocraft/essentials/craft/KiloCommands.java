package org.kilocraft.essentials.craft;


import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TestCommand;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.util.SomeGlobals;
import org.kilocraft.essentials.craft.commands.VersionCommand;
import org.kilocraft.essentials.craft.commands.essentials.*;
import org.kilocraft.essentials.craft.commands.essentials.ItemCommands.ItemCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ReloadCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ServerCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ServerModNameCommand;

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
            TestCommand.register(dispatcher);
        }

        VersionCommand.register(dispatcher);
        ReloadCommand.register(dispatcher);
        EnderchestCommand.register(dispatcher);
        TpaCommand.register(dispatcher);
        ServerCommand.register(dispatcher);
        ItemCommand.register(dispatcher);
        AnvilCommand.register(dispatcher);
        CraftingbenchCommand.register(dispatcher);
        LocateBiomeCommand.register(dispatcher);
        NickCommand.register(dispatcher);
        ServerModNameCommand.register(dispatcher);

    }

}
