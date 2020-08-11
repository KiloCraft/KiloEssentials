package org.kilocraft.essentials.extensions.betterchairs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.mixin.accessor.EntityAccessor;
import org.kilocraft.essentials.user.preference.Preferences;

import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;

public class SitCommand extends EssentialCommand {
    public SitCommand() {
        super("sit", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SIT_SELF), new String[]{"seat"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> enableArgument = literal("enable")
                .executes((ctx) -> set(ctx, true))
                .then(argument("target", player())
                        .requires(src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SIT_OTHERS))
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes((ctx) -> setOthers(ctx, true)));

        LiteralArgumentBuilder<ServerCommandSource> disableArgument = literal("disable")
                .executes((ctx) -> set(ctx, false))
                .then(argument("target", player())
                        .requires(src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SIT_OTHERS))
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes((ctx) -> setOthers(ctx, false)));

        argumentBuilder.executes(this::seat);
        commandNode.addChild(disableArgument.build());
        commandNode.addChild(enableArgument.build());
    }

    private int set(CommandContext<ServerCommandSource> ctx, boolean enable) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx.getSource());
        user.getPreferences().set(Preferences.CAN_SEAT, enable);

        if (user.getPreference(Preferences.CAN_SEAT)) {
            user.sendLangMessage("command.sit.enabled");
        } else {
            user.sendLangMessage("command.sit.disabled");
        }

        return SUCCESS;
    }

    private int seat(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx.getSource());

        if (SeatManager.getInstance().isSitting(user.asPlayer())) {
            SeatManager.getInstance().unseat(user);
            return FAILED;
        }

        if (!((EntityAccessor) user.asPlayer()).isOnGround()) {
            user.sendLangMessage("general.on_ground");
            return FAILED;
        }

        SeatManager.getInstance().seat(user, user.getLocationAsVector(), SeatManager.SummonType.COMMAND);
        return SUCCESS;
    }

    private int setOthers(CommandContext<ServerCommandSource> ctx, boolean set) throws CommandSyntaxException {
        OnlineUser target = getOnlineUser(getPlayer(ctx, "target").getCommandSource());

        target.getPreferences().set(Preferences.CAN_SEAT, set);
        KiloChat.sendLangMessageTo(ctx.getSource(), "template.#1", "canSit", set, target.getUsername());
        return SUCCESS;
    }

}
