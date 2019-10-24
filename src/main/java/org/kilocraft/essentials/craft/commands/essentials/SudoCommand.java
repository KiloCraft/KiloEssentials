package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.chat.KiloChat;

public class SudoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("sudo")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("sudo.others"), 3))
                .executes(c -> executeUsage(c.getSource()))
                .then(
                        CommandManager.argument("player", EntityArgumentType.player())
                            .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                            .executes(c -> executeUsage(c.getSource()))
                            .then(
                                    CommandManager.argument("command", StringArgumentType.greedyString())
                                            .executes(c -> execute(dispatcher, c.getSource(), EntityArgumentType.getPlayer(c, "player"), StringArgumentType.getString(c, "command")))
                            )
                );

        dispatcher.register(argumentBuilder);
    }

    private static int execute(CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, ServerPlayerEntity player, String command) {
        KiloChat.sendLangMessageTo(source, "command.sudo.success", player.getName().asString());

        if (command.startsWith("c:")) {
            KiloChat.sendChatMessage(player, command.replaceFirst("c:", ""));
        } else if (!command.contains("sudo")) {
            try {
                dispatcher.execute(command.replace("/", ""), player.getCommandSource());
            } catch (CommandSyntaxException e) {
                KiloChat.sendLangMessageTo(source, "command.sudo.failed", command);
            }
        } else
            KiloChat.sendLangMessageTo(source, "command.sudo.failed", "You can not loop into a sudo command");

        return 1;
    }

    private static int executeUsage(ServerCommandSource source) {
        KiloChat.sendLangMessageTo(source, "command.sudo.usage");
        return 1;
    }
}
