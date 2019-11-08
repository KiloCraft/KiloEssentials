package org.kilocraft.essentials.commands;

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

public class RainbowCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("rainbowcommand")
                .then(
                        CommandManager.argument("StringArgumentType#1", StringArgumentType.string())
                        .then(
                                CommandManager.argument("WordArgumentType#1", StringArgumentType.word())
                                .then(
                                        CommandManager.argument("IntegerArgumentType#1", IntegerArgumentType.integer())
                                                .then(
                                                        CommandManager.argument("StringArgumentType", StringArgumentType.string())
                                                                .then(
                                                                        CommandManager.argument("WordArgumentType#2", StringArgumentType.word())
                                                                                .then(
                                                                                        CommandManager.argument("IntegerArgumentType#2", IntegerArgumentType.integer())
                                                                                        .then(
                                                                                                CommandManager.argument("blockPos", BlockPosArgumentType.blockPos())
                                                                                                .then(
                                                                                                        CommandManager.argument("DoubleArgumentType", DoubleArgumentType.doubleArg())
                                                                                                            .then(
                                                                                                                    CommandManager.argument("Vec3ArgumentType", Vec3ArgumentType.vec3())
                                                                                                                        .then(
                                                                                                                                CommandManager.argument("BoolArgumentType", BoolArgumentType.bool())
                                                                                                                                    .executes(context -> {
                                                                                                                                        context.getSource().sendFeedback(
                                                                                                                                                new LiteralText("Well... you tried! but this is just a test command"),
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
