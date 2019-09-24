package org.kilocraft.essentials.craft;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.SharedConstants;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.util.SomeGlobals;
import org.kilocraft.essentials.craft.commands.VersionCommand;
import org.kilocraft.essentials.craft.commands.essentials.*;
import org.kilocraft.essentials.craft.commands.essentials.ItemCommands.ItemCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ReloadCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ServerModNameCommand;
import org.kilocraft.essentials.craft.config.KiloConfig;

import java.util.Map;

public class KiloCommands {
    private static CommandDispatcher<ServerCommandSource> dispatcher;
    public KiloCommands(boolean dev) {
        dispatcher = SomeGlobals.commandDispatcher;
        // Thimble.registerDispatcherCommands(dispatcher); uncomment when we have a custom dispatcher
        register(dev);
    }

    private static void register(boolean devEnv) {
        if (devEnv) {
            Mod.getLogger().debug("Server is running in debug mode!");
            SharedConstants.isDevelopment = true;
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

    public static String buildSmartUsage(LiteralCommandNode<ServerCommandSource> literalCommandNode, ServerCommandSource source) {
        String string = KiloConfig.getMessages().get("commands.usage");
        Map<CommandNode<ServerCommandSource>, String> usage = dispatcher.getSmartUsage(literalCommandNode, source);
        return string.replaceFirst("%s", usage.toString());
    }

    public static String buildUsage(String usage, ServerCommandSource source) {
        String string = KiloConfig.getMessages().get("commands.usage");
        return string.replaceFirst("%s", usage);
    }
}
