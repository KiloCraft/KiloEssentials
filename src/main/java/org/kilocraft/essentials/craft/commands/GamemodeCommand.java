package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.Collection;

public class GamemodeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> fullLiteral = CommandManager.literal("gamemode");
        LiteralArgumentBuilder<ServerCommandSource> shortLiteral = CommandManager.literal("gm");
        LiteralArgumentBuilder<ServerCommandSource> shortCommand;
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetArgument = CommandManager.argument("target", EntityArgumentType.player());


    }

    private static GameMode[] gameModes = GameMode.values();
    private static int var = gameModes.length;

    private static void buildFullLiteral(LiteralArgumentBuilder<ServerCommandSource> builder) {
//        for (int i = 0; i < var; i++) {
//            GameMode mode = gameModes[var];
//            if (mode != GameMode.NOT_SET) {
//            }
//        }

    }

    private static void execute(Collection<ServerPlayerEntity> playerEntities, GameMode gameMode) {
    }

}
