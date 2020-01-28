package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.inventory.ServerUserInventory;

import static org.kilocraft.essentials.KiloCommands.SUCCESS;

public class EnderchestCommand extends EssentialCommand {
    public EnderchestCommand() {
        super("enderchest", CommandPermission.ENDERCHEST_SELF, new String[]{"ec"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("target", EntityArgumentType.player())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.ENDERCHEST_OTHERS))
                .suggests(TabCompletions::allPlayers)
                .executes(ctx -> execute(ctx.getSource().getPlayer(), EntityArgumentType.getPlayer(ctx, "target")));

        argumentBuilder.executes(ctx -> execute(ctx.getSource().getPlayer(), ctx.getSource().getPlayer()));
        commandNode.addChild(selectorArgument.build());
    }

    private static int execute(ServerPlayerEntity sender, ServerPlayerEntity target) {
        ServerUserInventory.openEnderchest(sender, target);
        if (CommandHelper.areTheSame(sender, target))
            KiloChat.sendLangMessageTo(sender, "general.open_container", "Ender chest");
        else
            KiloChat.sendLangMessageTo(sender, "general.seek_container", target.getEntityName(), "Ender chest");

        return SUCCESS();
    }

}
