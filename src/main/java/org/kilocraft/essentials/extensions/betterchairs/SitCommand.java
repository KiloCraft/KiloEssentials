package org.kilocraft.essentials.extensions.betterchairs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.mixin.accessor.EntityAccessor;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.EssentialPermission;

import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;

public class SitCommand extends EssentialCommand {
    public SitCommand() {
        super("sit", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SIT_SELF), new String[]{"seat"});
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> enableArgument = this.literal("enable")
                .executes((ctx) -> this.set(ctx, true))
                .then(this.argument("target", player())
                        .requires(src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SIT_OTHERS))
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes((ctx) -> this.setOthers(ctx, true)));

        LiteralArgumentBuilder<CommandSourceStack> disableArgument = this.literal("disable")
                .executes((ctx) -> this.set(ctx, false))
                .then(this.argument("target", player())
                        .requires(src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SIT_OTHERS))
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes((ctx) -> this.setOthers(ctx, false)));

        this.argumentBuilder.executes(this::seat);
        this.commandNode.addChild(disableArgument.build());
        this.commandNode.addChild(enableArgument.build());
    }

    private int set(CommandContext<CommandSourceStack> ctx, boolean enable) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);
        user.getPreferences().set(Preferences.CAN_SEAT, enable);

        if (user.getPreference(Preferences.CAN_SEAT)) {
            user.sendLangMessage("command.sit.enabled");
        } else {
            user.sendLangMessage("command.sit.disabled");
        }

        return SUCCESS;
    }

    private int seat(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);

        if (SeatManager.getInstance().isSitting(user.asPlayer())) {
            SeatManager.getInstance().unseat(user.asPlayer());
            return FAILED;
        }

        if (!((EntityAccessor) user.asPlayer()).isOnGround()) {
            user.sendLangMessage("general.on_ground");
            return FAILED;
        }

        SeatManager.getInstance().seat(user, user.getLocationAsVector(), SeatManager.SummonType.COMMAND);
        return SUCCESS;
    }

    private int setOthers(CommandContext<CommandSourceStack> ctx, boolean set) throws CommandSyntaxException {
        CommandSourceUser user = this.getCommandSource(ctx);
        OnlineUser target = this.getOnlineUser(getPlayer(ctx, "target"));

        target.getPreferences().set(Preferences.CAN_SEAT, set);
        user.sendLangMessage("template.#1", "canSit", set, target.getUsername());
        return SUCCESS;
    }

}
