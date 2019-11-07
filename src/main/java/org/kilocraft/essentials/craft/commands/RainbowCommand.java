package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.command.arguments.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.arguments.BlockPosArgumentType.blockPos;
import static net.minecraft.command.arguments.Vec3ArgumentType.vec3;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RainbowCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("rainbowcommand")
                .then(argument("StringArgumentType#1", string())
                        .then(argument("WordArgumentType#1", word())
                                .then(argument("IntegerArgumentType#1", integer())
                                                .then(argument("StringArgumentType", string())
                                                                .then(argument("WordArgumentType#2", word())
                                                                                .then(argument("IntegerArgumentType#2", integer())
                                                                                        .then(argument("blockPos", blockPos())
                                                                                                .then(argument("DoubleArgumentType", doubleArg())
                                                                                                            .then(argument("Vec3ArgumentType", vec3())
                                                                                                                        .then(argument("BoolArgumentType", bool())
                                                                                                                                    .executes(context -> {
                                                                                                                                        context.getSource().sendFeedback(
                                                                                                                                                new LiteralText("Well... you tried! but this is just a test command"),  // TODO Magic value
                                                                                                                                                false
                                                                                                                                        );
                                                                                                                                        return 1;
                                                                                                                                    })
                                                                                                                        )
                                                                                                            )
                                                                                                )
                                                                                        )
                                                                                )
                                                                )
                                                )
                                )
                        )
                );

        dispatcher.register(argumentBuilder);
    }
}
