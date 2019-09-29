package org.kilocraft.essentials.craft;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.SharedConstants;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.util.SomeGlobals;
import org.kilocraft.essentials.craft.commands.GamemodeCommand;
import org.kilocraft.essentials.craft.commands.VersionCommand;
import org.kilocraft.essentials.craft.commands.essentials.*;
import org.kilocraft.essentials.craft.commands.essentials.ItemCommands.ItemCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ReloadCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ServerCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ServerModNameCommand;
import org.kilocraft.essentials.craft.config.KiloConfig;

import java.util.Map;

public class KiloCommands {
    private static CommandDispatcher<ServerCommandSource> dispatcher;
    public KiloCommands() {
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
        register(false);
    }

    private static void register(boolean devEnv) {
        if (devEnv) {
            Mod.getLogger().debug("Server is running in debug mode!");
            SharedConstants.isDevelopment = true;
        }

        VersionCommand.register(dispatcher);
        ReloadCommand.register(dispatcher);
        GamemodeCommand.register(dispatcher);
        EnderchestCommand.register(dispatcher);
        TpaCommand.register(dispatcher);
        ServerCommand.register(dispatcher);
        ItemCommand.register(dispatcher);
        AnvilCommand.register(dispatcher);
        CraftingbenchCommand.register(dispatcher);
        LocateBiomeCommand.register(dispatcher);
        NickCommand.register(dispatcher);
        ServerModNameCommand.register(dispatcher);
        ColoursCommand.register(dispatcher);
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
