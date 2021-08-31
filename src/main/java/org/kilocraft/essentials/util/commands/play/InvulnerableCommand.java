package org.kilocraft.essentials.util.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;

public class InvulnerableCommand extends EssentialCommand {
    public InvulnerableCommand() {
        super("invulnerable", CommandPermission.INVULNERAVLE, new String[]{"godmode"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("player", player())
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> executeToggle(ctx, getPlayer(ctx, "player")));

        RequiredArgumentBuilder<ServerCommandSource, Boolean> setArgument = argument("set", bool())
                .executes(c -> executeSet(c, getPlayer(c, "player"), getBool(c, "set")));

        argumentBuilder.executes(ctx -> executeToggle(ctx, ctx.getSource().getPlayer()));
        selectorArgument.then(setArgument);
        commandNode.addChild(selectorArgument.build());
    }

    private int executeToggle(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player) {
        executeSet(ctx, player, !player.isInvulnerable());
        return 1;
    }

    private int executeSet(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, boolean set) {
        player.setInvulnerable(set);
        ServerCommandSource source = ctx.getSource();
        CommandSourceUser commandSource = getCommandSource(ctx);
        commandSource.sendLangMessage("template.#1", "Invulnerable", set, player.getName().asString());

        OnlineUser user = KiloEssentials.getUserManager().getOnline(player);
        user.getPreferences().set(Preferences.INVULNERABLE, set);

        if (!CommandUtils.areTheSame(source, player))
            user.sendLangMessage("template.#1.announce", source.getName(), "Invulnerable", set);

        player.sendAbilitiesUpdate();
        return 1;
    }
}
