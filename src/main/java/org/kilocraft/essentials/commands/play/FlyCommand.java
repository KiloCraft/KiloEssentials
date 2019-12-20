package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.literal;

public class FlyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("fly")
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.FLY_SELF))
                .executes(c -> toggle(c.getSource(), c.getSource().getPlayer()))
                .then(
                        CommandManager.argument("player", player())
                                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.FLY_OTHERS))
                                .suggests(TabCompletions::allPlayers)
                                .executes(c -> toggle(c.getSource(), getPlayer(c, "player")))
                                .then(
                                        CommandManager.argument("set", bool())
                                                .executes(c -> execute(c.getSource(), getPlayer(c, "player"), getBool(c, "set")))
                                )
                );

        dispatcher.register(argumentBuilder);
    }

    private static int toggle(ServerCommandSource source, ServerPlayerEntity playerEntity) {
        execute(source, playerEntity, !playerEntity.abilities.allowFlying);
        return 1;
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity playerEntity, boolean bool) {
        if (!playerEntity.abilities.allowFlying == bool) {
            playerEntity.abilities.allowFlying = bool;
            playerEntity.abilities.flying = bool;
            playerEntity.sendAbilitiesUpdate();
            
            OnlineUser user = KiloServer.getServer().getUserManager().getOnline(playerEntity);
            user.setFlight(bool);

            KiloChat.sendLangMessageTo(source, "template.#1", "Flight", bool, playerEntity.getName().asString());

            if (!CommandHelper.areTheSame(source, playerEntity))
                KiloChat.sendLangMessageTo(playerEntity, "template.#1.announce", source.getName(), "Flight", bool);
        }

        return 1;
    }
}
