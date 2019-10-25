package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.util.CommandHelper;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.chat.KiloChat;

public class InvulnerablemodeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("invulnerable")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("invulnerable"), 2))
                .executes(c -> executeToggle(c.getSource(), c.getSource().getPlayer()))
                .then(
                        CommandManager.argument("player", EntityArgumentType.player())
                            .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                            .executes(c -> executeToggle(c.getSource(), EntityArgumentType.getPlayer(c, "player")))
                            .then(
                                    CommandManager.argument("set", BoolArgumentType.bool())
                                        .executes(c -> executeSet(c.getSource(), EntityArgumentType.getPlayer(c, "player"), BoolArgumentType.getBool(c, "set")))
                            )
                );

        dispatcher.register(argumentBuilder);
    }

    private static int executeToggle(ServerCommandSource source, ServerPlayerEntity player) {
        executeSet(source, player, !player.isInvulnerable());
        return 1;
    }

    private static int executeSet(ServerCommandSource source, ServerPlayerEntity player, boolean set) {
        player.setInvulnerable(set);
        KiloChat.sendLangMessageTo(source, "template.#1", "Invulnerable", set, player.getName().asString());

        if (!CommandHelper.areTheSame(source, player))
            KiloChat.sendLangMessageTo(player, "template.#1.announce", source.getName(), "Invulnerable", set);

        player.sendAbilitiesUpdate();
        return 1;
    }
}
