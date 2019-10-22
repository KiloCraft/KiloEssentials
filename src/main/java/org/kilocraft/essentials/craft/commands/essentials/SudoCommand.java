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
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("sudo"), 2))
                .then(
                        CommandManager.argument("player", EntityArgumentType.player())
                            .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                            .then(
                                    CommandManager.argument("command", StringArgumentType.greedyString())
                                            .executes(c -> execute(dispatcher, c.getSource(), EntityArgumentType.getPlayer(c, "player"), StringArgumentType.getString(c, "command")))
                            )
                );

        dispatcher.register(argumentBuilder);
    }

    private static int execute(CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, ServerPlayerEntity player, String command) {
        try {
            dispatcher.execute(command, player.getCommandSource());
            KiloChat.sendLangMessageTo(source, "command.sudo.success", player.getName().asString());
        } catch (CommandSyntaxException e) {
            KiloChat.sendLangMessageTo(source, "command.sudo.failed", e.getMessage());
        }

        return 1;
    }
}
