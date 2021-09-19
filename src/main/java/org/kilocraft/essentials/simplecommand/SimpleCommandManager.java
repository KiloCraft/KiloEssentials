package org.kilocraft.essentials.simplecommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.util.commands.KiloCommands;

import java.util.ArrayList;
import java.util.List;

public class SimpleCommandManager {
    private static final List<SimpleCommand> commands = new ArrayList<>();

    public static void register(final SimpleCommand command) {
        commands.add(command);

        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal(command.getLabel())
                .requires(src -> canUse(src, command));

        if (command.hasArgs) {
            builder.then(CommandManager.argument("args", StringArgumentType.greedyString())
                    .suggests(ArgumentSuggestions::noSuggestions)
                    .executes(context -> command.executable.execute(context.getSource(), StringArgumentType.getString(context, "args").split(" ")))
            );
        }
        builder.executes(context -> command.executable.execute(context.getSource(), new String[0]));

        KiloCommands.getDispatcher().register(builder);
    }

    private static boolean canUse(ServerCommandSource src, SimpleCommand command) {
        boolean canUse = true;
        if (command.opReq != 0) {
            canUse = src.hasPermissionLevel(command.opReq);
        }

        if (command.permReq != null && !command.permReq.isEmpty()) {
            canUse = canUse || KiloCommands.hasPermission(src, command.permReq);
        }

        return canUse;
    }

    public static void unregister(final String id) {
        ((CommandNodeInterface) KiloCommands.getDispatcher().getRoot()).removeLiteral(id);
    }

    public static List<SimpleCommand> getCommands() {
        return commands;
    }

}
