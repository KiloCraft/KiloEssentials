package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandUtils;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;

public class FlyCommand extends EssentialCommand {
    public FlyCommand() {
        super("fly", CommandPermission.FLY_SELF, new String[]{"flight"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("player", player())
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.FLY_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(c -> toggle(c.getSource(), getPlayer(c, "player")));

        RequiredArgumentBuilder<ServerCommandSource, Boolean> setArgument = argument("set", bool())
                .executes(c -> execute(c.getSource(), getPlayer(c, "player"), getBool(c, "set")));

        selectorArgument.then(setArgument);
        commandNode.addChild(selectorArgument.build());
        argumentBuilder.executes(ctx -> toggle(ctx.getSource(), ctx.getSource().getPlayer()));
    }

    private static int toggle(ServerCommandSource source, ServerPlayerEntity playerEntity) {
        return execute(source, playerEntity, !playerEntity.abilities.allowFlying);
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity playerEntity, boolean bool) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(playerEntity);
        user.setFlight(bool);

        KiloChat.sendLangMessageTo(source, "template.#1", "Flight", bool, playerEntity.getName().asString());

        if (!CommandUtils.areTheSame(source, playerEntity))
            KiloChat.sendLangMessageTo(playerEntity, "template.#1.announce", source.getName(), "Flight", bool);

        return 1;
    }

}
