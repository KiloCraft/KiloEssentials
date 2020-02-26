package org.kilocraft.essentials.extensions.betterchairs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.mixin.accessor.EntityAccessor;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;

public class SitCommand extends EssentialCommand {
    public SitCommand() {
        super("sit", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SIT_SELF));
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> boolArgument =  argument("set", word())
                .suggests(TabCompletions::stateSuggestions)
                .executes(this::set);

        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArg = argument("target", player())
                .requires(src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SIT_OTHERS))
                .suggests(TabCompletions::allPlayers)
                .executes(this::setOthers);

        argumentBuilder.executes(this::seat);
        boolArgument.then(selectorArg);
        commandNode.addChild(boolArgument.build());
    }

    private int set(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx.getSource());
        String input = getString(ctx, "set");
        user.setCanSit(input.equalsIgnoreCase("toggle") ? !user.canSit() : input.equals("on"));

        if (user.canSit())
            user.sendLangMessage("command.sit.enabled");
        else
            user.sendLangMessage("command.sit.disabled");
        return SINGLE_SUCCESS;
    }

    private int seat(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx.getSource());

        if (PlayerSitManager.INSTANCE.isSitting(user.getPlayer())) {
            PlayerSitManager.INSTANCE.sitOff(user.getPlayer());
            return -1;
        }

        if (!((EntityAccessor) user.getPlayer()).isOnGround()) {
            user.sendLangMessage("general.on_ground");
            return -1;
        }

        PlayerSitManager.INSTANCE.sitOn(user.getPlayer(), user.getLocationAsVector(), PlayerSitManager.SummonType.COMMAND, false);
        return SINGLE_SUCCESS;
    }

    private int setOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser target = getOnlineUser(getPlayer(ctx, "target").getCommandSource());
        String input = getString(ctx, "set");
        boolean bool = input.equals("on");

        target.setCanSit(bool);
        KiloChat.sendLangMessageTo(ctx.getSource(), "template.#1", "canSit", bool, target.getUsername());
        return SINGLE_SUCCESS;
    }

}
