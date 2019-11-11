package org.kilocraft.essentials.commands.help;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.commands.SuggestArgument;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class UsageCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> usageCommand = dispatcher.register(literal("usage").then(
                        argument("command", greedyString())
                                .suggests(SuggestArgument::usableCommands)
                                .executes(context -> execute(context.getSource(), getString(context, "command")))
                )
        );

        dispatcher.getRoot().addChild(usageCommand);
    }

    private static int execute(ServerCommandSource source, String command) throws CommandSyntaxException {
        String fromLang = ModConstants.getLang().getProperty("command." + command + ".usage");

        if (fromLang != null)
            KiloCommands.executeUsageFor("command." + command + ".usage", source);
        else
            KiloCommands.executeSmartUsageFor(command, source);

        return KiloCommands.SUCCESS();
    }

}
