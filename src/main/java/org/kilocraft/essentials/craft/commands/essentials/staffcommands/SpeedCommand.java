package org.kilocraft.essentials.craft.commands.essentials.staffcommands;

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

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SpeedCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("speed")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed"), 2));

        LiteralArgumentBuilder<ServerCommandSource> walkSpeed = literal("walk")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.walk.self"), 2))
                .then(
                        literal("set").then(
                                argument("walkSpeed", floatArg(0.0F, 10.0F))
                                        .executes(c -> executeSet(true, c.getSource(), c.getSource().getPlayer(), getFloat(c, "walkSpeed")))
                                        .then(
                                                argument("player", player())
                                                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.walk.others"), 2))
                                                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                                        .executes(
                                                                c -> executeSet(true, c.getSource(), getPlayer(c, "player"), getFloat(c, "walkSpeed"))
                                                        )

                                        )
                        )
                )
                .then(
                        literal("reset")
                                .executes(c -> executeReset(true, c.getSource(), c.getSource().getPlayer()))
                                .then(
                                argument("player", player())
                                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.walk.others"), 2))
                                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                        .executes(
                                                c -> executeReset(true, c.getSource(), getPlayer(c, "player"))
                                        )
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> flightSpeed = literal("flight")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.flight.self"), 2))
                .then(
                        literal("set").then(
                                argument("flightSpeed", floatArg(0.0F, 10.0F))
                                        .executes(c -> executeSet(false, c.getSource(), c.getSource().getPlayer(), getFloat(c, "flightSpeed")))
                                        .then(
                                                argument("player", player())
                                                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.flight.others"), 2))
                                                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                                        .executes(
                                                                c -> executeSet(false, c.getSource(), getPlayer(c, "player"), getFloat(c, "flightSpeed"))
                                                        )

                                        )
                        )
                )
                .then(
                        literal("reset")
                                .executes(c -> executeReset(false, c.getSource(), c.getSource().getPlayer()))
                                .then(
                                argument("player", player())
                                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("speed.flight.others"), 2))
                                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                        .executes(
                                                c -> executeReset(false, c.getSource(), getPlayer(c, "player"))
                                        )
                        )
                );

        argumentBuilder.then(walkSpeed);
        argumentBuilder.then(flightSpeed);

        dispatcher.register(argumentBuilder);
    }

    private static int executeSet(boolean walkSpeed, ServerCommandSource source, ServerPlayerEntity target, float speed) {
        if (walkSpeed)
            target.setMovementSpeed(speed);
        else
            target.flyingSpeed = speed;

        target.sendAbilitiesUpdate();

        target.sendAbilitiesUpdate();
        target.setSneaking(true);

        KiloChat.sendLangMessageTo(source, "command.speed.set", walkSpeed ? "walk" : "flight", speed, target.getName().asString());
        return 1;
    }

    private static int executeReset(boolean walkSpeed, ServerCommandSource source, ServerPlayerEntity target) {
        if (walkSpeed)
            target.setMovementSpeed(1.0F);
        else
            target.flyingSpeed = 0.02F;

        target.sendAbilitiesUpdate();

        KiloChat.sendLangMessageTo(source, "command.speed.set", walkSpeed ? "walk" : "flight", "reset &8(&a1.0F&8)&r", target.getName().asString());
        return 1;
    }

    private static void setWalkSpeed(ServerPlayerEntity player, float speed) {
    }

}