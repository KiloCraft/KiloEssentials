package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.util.text.Texter;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;

public class EnderchestCommand extends EssentialCommand {
    public EnderchestCommand() {
        super("enderchest", CommandPermission.ENDERCHEST_SELF, new String[]{"ec", "ender"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("target", player())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.ENDERCHEST_OTHERS))
                .suggests(ArgumentCompletions::allPlayers)
                .executes(ctx -> execute(ctx.getSource().getPlayer(), getPlayer(ctx, "target")));

        argumentBuilder.executes(ctx -> execute(ctx.getSource().getPlayer(), ctx.getSource().getPlayer()));
        commandNode.addChild(selectorArgument.build());
    }

    private int execute(ServerPlayerEntity sender, ServerPlayerEntity target) {
        OnlineUser src = this.getOnlineUser(sender);
        OnlineUser targetUser = this.getOnlineUser(target);
        Text text;
        Text translatable =  new TranslatableText("container.enderchest");

        if (sender.equals(target)) {
            text = Texter.toText().append(translatable).append(" ").append(targetUser.getFormattedDisplayName());
        } else {
            text = translatable;
        }

        EnderChestInventory enderChestInventory = target.getEnderChestInventory();
        sender.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, pInv, pEntity) ->
                GenericContainerScreenHandler.createGeneric9x3(syncId, pInv, enderChestInventory), text)
        );

        if (CommandUtils.areTheSame(sender, target)) {
            src.sendLangMessage("general.open_screen", "Ender Chest");
        } else {
            src.sendLangMessage("general.seek_screen", target.getEntityName(), "Ender Chest");
        }

        return SUCCESS;
    }

}
