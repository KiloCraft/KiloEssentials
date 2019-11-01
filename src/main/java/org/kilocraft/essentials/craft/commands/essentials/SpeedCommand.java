package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.chat.KiloChat;

public class SpeedCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("speed")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed"), 2));

        LiteralArgumentBuilder<ServerCommandSource> walkSpeed = CommandManager.literal("walk")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.walk.self"), 2))
                .then(
                        CommandManager.literal("set").then(
                                CommandManager.argument("walkSpeed", FloatArgumentType.floatArg(0.0F, 10.0F))
                                        .executes(c -> executeSet(true, c.getSource(), c.getSource().getPlayer(), FloatArgumentType.getFloat(c, "walkSpeed")))
                                        .then(
                                                CommandManager.argument("player", EntityArgumentType.player())
                                                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.walk.others"), 2))
                                                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                                        .executes(
                                                                c -> executeSet(true, c.getSource(), EntityArgumentType.getPlayer(c, "player"), FloatArgumentType.getFloat(c, "walkSpeed"))
                                                        )

                                        )
                        )
                )
                .then(
                        CommandManager.literal("reset")
                                .executes(c -> executeReset(true, c.getSource(), c.getSource().getPlayer()))
                                .then(
                                CommandManager.argument("player", EntityArgumentType.player())
                                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.walk.others"), 2))
                                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                        .executes(
                                                c -> executeReset(true, c.getSource(), EntityArgumentType.getPlayer(c, "player"))
                                        )
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> flightSpeed = CommandManager.literal("flight")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.flight.self"), 2))
                .then(
                        CommandManager.literal("set").then(
                                CommandManager.argument("flightSpeed", FloatArgumentType.floatArg(0.0F, 10.0F))
                                        .executes(c -> executeSet(false, c.getSource(), c.getSource().getPlayer(), FloatArgumentType.getFloat(c, "flightSpeed")))
                                        .then(
                                                CommandManager.argument("player", EntityArgumentType.player())
                                                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.flight.others"), 2))
                                                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                                        .executes(
                                                                c -> executeSet(false, c.getSource(), EntityArgumentType.getPlayer(c, "player"), FloatArgumentType.getFloat(c, "flightSpeed"))
                                                        )

                                        )
                        )
                )
                .then(
                        CommandManager.literal("reset")
                                .executes(c -> executeReset(false, c.getSource(), c.getSource().getPlayer()))
                                .then(
                                CommandManager.argument("player", EntityArgumentType.player())
                                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.flight.others"), 2))
                                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                        .executes(
                                                c -> executeReset(false, c.getSource(), EntityArgumentType.getPlayer(c, "player"))
                                        )
                        )
                );

        argumentBuilder.then(walkSpeed);
        argumentBuilder.then(flightSpeed);

        dispatcher.register(argumentBuilder);
    }

    private static int executeSet(boolean walkSpeed, ServerCommandSource source, ServerPlayerEntity target, float speed) {
        if (walkSpeed) {
            target.setMovementSpeed(speed);
        } else {
            target.abilities.setFlySpeed(speed);
        }
            
        target.sendAbilitiesUpdate();

        target.sendAbilitiesUpdate();
        target.setSneaking(true);

        KiloChat.sendLangMessageTo(source, "command.speed.set", walkSpeed ? "walk" : "flight", speed, target.getName().asString());
        return 1;
    }

    private static int executeReset(boolean walkSpeed, ServerCommandSource source, ServerPlayerEntity target) {
        if (walkSpeed) {
            target.setMovementSpeed(1.0F);
        } else {
        	target.abilities.setFlySpeed(0.02f);
        }
            
        target.sendAbilitiesUpdate();

        KiloChat.sendLangMessageTo(source, "command.speed.set", walkSpeed ? "walk" : "flight", "reset &8(&a1.0F&8)&r", target.getName().asString());
        return 1;
    }

}