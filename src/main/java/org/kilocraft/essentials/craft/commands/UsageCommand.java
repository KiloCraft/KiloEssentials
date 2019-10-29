package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.KiloCommands;

public class UsageCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("usage").then(
                CommandManager.argument("command", StringArgumentType.greedyString())
                        .executes(context -> execute(context.getSource(), StringArgumentType.getString(context, "command")))
        );

        dispatcher.register(argumentBuilder);
    }

    private static int execute(ServerCommandSource source, String command) throws CommandSyntaxException {
        String fromLang = Mod.getLang().getProperty("command." + command + ".usage");

        if (fromLang != null)
            KiloCommands.executeUsageFor("command." + command + ".usage", source);
        else
            KiloCommands.executeSmartUsageFor(command, source);

        return 1;
    }

}
