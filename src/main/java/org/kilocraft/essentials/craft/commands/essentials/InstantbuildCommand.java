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

public class InstantbuildCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("instabuild")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("instabuild.self"), 3))
                .executes(context -> executeToggle(context.getSource(), context.getSource().getPlayer()))
                .then(
                        CommandManager.argument("player", EntityArgumentType.player())
                                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("instabuild.others"), 3))
                                .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                .executes(context -> executeToggle(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                                .then(
                                        CommandManager.argument("set", BoolArgumentType.bool())
                                            .executes(context -> executeSet(context.getSource(), EntityArgumentType.getPlayer(context, "player"), BoolArgumentType.getBool(context, "set")))
                                )
                );

        dispatcher.register(argumentBuilder);
    }

    private static int executeToggle(ServerCommandSource source, ServerPlayerEntity target) {
        return executeSet(source, target, !target.abilities.creativeMode);
    }

    private static int executeSet(ServerCommandSource source, ServerPlayerEntity target, boolean set) {
        target.abilities.creativeMode = set;
        target.sendAbilitiesUpdate();

        KiloChat.sendLangMessageTo(source, "template.#1", "Instant build", set, target.getName().asString());

        if (!CommandHelper.areTheSame(source, target))
            KiloChat.sendLangMessageTo(target, "template.#1.announce", source.getName(), "Instant build", set);

        return 1;
    }
}
