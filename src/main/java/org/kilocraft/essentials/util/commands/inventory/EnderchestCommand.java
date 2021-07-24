package org.kilocraft.essentials.util.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.text.Texter;

public class EnderchestCommand extends EssentialCommand {
    public EnderchestCommand() {
        super("enderchest", CommandPermission.ENDERCHEST_SELF, new String[]{"ec", "ender"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("target", EntityArgumentType.player())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.ENDERCHEST_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> execute(ctx.getSource().getPlayer(), EntityArgumentType.getPlayer(ctx, "target")));

        argumentBuilder.executes(ctx -> execute(ctx.getSource().getPlayer(), ctx.getSource().getPlayer()));
        commandNode.addChild(selectorArgument.build());
    }

    private int execute(ServerPlayerEntity sender, ServerPlayerEntity target) {
        OnlineUser targetUser = this.getOnlineUser(target);
        Text text;
        Text translatable =  new TranslatableText("container.enderchest");

        if (!sender.equals(target)) {
            text = Texter.newText().append(translatable).append(" ").append(targetUser.getFormattedDisplayName());
        } else {
            text = translatable;
        }

        EnderChestInventory enderChestInventory = target.getEnderChestInventory();
        sender.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, pInv, pEntity) ->
                GenericContainerScreenHandler.createGeneric9x3(syncId, pInv, enderChestInventory), text)
        );

        return SUCCESS;
    }

}
