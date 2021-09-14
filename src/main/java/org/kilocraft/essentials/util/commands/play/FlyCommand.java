package org.kilocraft.essentials.util.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.commands.KiloCommands;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;

public class FlyCommand extends EssentialCommand {
    public FlyCommand() {
        super("fly", CommandPermission.FLY_SELF, new String[]{"flight"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = this.argument("player", player())
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.FLY_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(c -> this.toggle(c.getSource(), getPlayer(c, "player")));

        RequiredArgumentBuilder<ServerCommandSource, Boolean> setArgument = this.argument("set", bool())
                .executes(c -> this.execute(c.getSource(), getPlayer(c, "player"), getBool(c, "set")));

        selectorArgument.then(setArgument);
        this.commandNode.addChild(selectorArgument.build());
        this.argumentBuilder.executes(ctx -> this.toggle(ctx.getSource(), ctx.getSource().getPlayer()));
    }

    private int toggle(ServerCommandSource source, ServerPlayerEntity playerEntity) {
        return this.execute(source, playerEntity, !playerEntity.getAbilities().allowFlying);
    }

    private int execute(ServerCommandSource source, ServerPlayerEntity playerEntity, boolean bool) {
        OnlineUser user = this.getUserManager().getOnline(playerEntity);
        user.setFlight(bool);

        user.sendLangMessage("template.#1", "Flight", bool, playerEntity.getName().asString());

        if (!CommandUtils.areTheSame(source, playerEntity))
            user.sendLangMessage("template.#1.announce", source.getName(), "Flight", bool);

        return 1;
    }

}
