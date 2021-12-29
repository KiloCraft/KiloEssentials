package org.kilocraft.essentials.util.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
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
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;

public class InvulnerableCommand extends EssentialCommand {
    public InvulnerableCommand() {
        super("invulnerable", CommandPermission.INVULNERAVLE, new String[]{"godmode"});
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> selectorArgument = this.argument("player", player())
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> this.executeToggle(ctx, getPlayer(ctx, "player")));

        RequiredArgumentBuilder<CommandSourceStack, Boolean> setArgument = this.argument("set", bool())
                .executes(c -> this.executeSet(c, getPlayer(c, "player"), getBool(c, "set")));

        this.argumentBuilder.executes(ctx -> this.executeToggle(ctx, ctx.getSource().getPlayerOrException()));
        selectorArgument.then(setArgument);
        this.commandNode.addChild(selectorArgument.build());
    }

    private int executeToggle(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        this.executeSet(ctx, player, !player.isInvulnerable());
        return 1;
    }

    private int executeSet(CommandContext<CommandSourceStack> ctx, ServerPlayer player, boolean set) {
        player.setInvulnerable(set);
        CommandSourceStack source = ctx.getSource();
        CommandSourceUser commandSource = this.getCommandSource(ctx);
        commandSource.sendLangMessage("template.#1", "Invulnerable", set, player.getName().getContents());

        OnlineUser user = KiloEssentials.getUserManager().getOnline(player);
        user.getPreferences().set(Preferences.INVULNERABLE, set);

        if (!CommandUtils.areTheSame(source, player))
            user.sendLangMessage("template.#1.announce", source.getTextName(), "Invulnerable", set);

        player.onUpdateAbilities();
        return 1;
    }
}
