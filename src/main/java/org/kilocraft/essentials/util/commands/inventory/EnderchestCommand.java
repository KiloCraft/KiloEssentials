package org.kilocraft.essentials.util.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.text.Texter;

public class EnderchestCommand extends EssentialCommand {
    public EnderchestCommand() {
        super("enderchest", CommandPermission.ENDERCHEST_SELF, new String[]{"ec", "ender"});
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> selectorArgument = this.argument("target", EntityArgument.player())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.ENDERCHEST_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> this.execute(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "target")));

        this.argumentBuilder.executes(ctx -> this.execute(ctx.getSource().getPlayerOrException(), ctx.getSource().getPlayerOrException()));
        this.commandNode.addChild(selectorArgument.build());
    }

    private int execute(ServerPlayer sender, ServerPlayer target) {
        OnlineUser targetUser = this.getOnlineUser(target);
        Component text;
        Component translatable = new TranslatableComponent("container.enderchest");

        if (!sender.equals(target)) {
            text = Texter.newText().append(translatable).append(" ").append(targetUser.getFormattedDisplayName());
        } else {
            text = translatable;
        }

        PlayerEnderChestContainer enderChestInventory = target.getEnderChestInventory();
        sender.openMenu(new SimpleMenuProvider((syncId, pInv, pEntity) ->
                ChestMenu.threeRows(syncId, pInv, enderChestInventory), text)
        );

        return SUCCESS;
    }

}
