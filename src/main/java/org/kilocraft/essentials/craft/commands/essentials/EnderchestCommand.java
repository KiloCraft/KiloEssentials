package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.container.GenericContainer;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class EnderchestCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(
                CommandManager.literal("enderchest")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> execute(context, EntityArgumentType.getPlayer(context, "player").getCommandSource())))
                        .executes(context -> execute(context, context.getSource()))
        );

        dispatcher.register(CommandManager.literal("ec")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> execute(context, context.getSource())));
    }

    private static int execute(CommandContext<ServerCommandSource> context, ServerCommandSource source) throws CommandSyntaxException {
        LiteralText literalText = new LiteralText("");
        ServerPlayerEntity target = source.getPlayer();
        ServerPlayerEntity sender = context.getSource().getPlayer();
        literalText.append("You have opened ").setStyle(new Style().setColor(Formatting.YELLOW));
        literalText.append(new LiteralText(target.getName().getString()).setStyle(new Style().setColor(Formatting.GOLD)
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new LiteralText(target.getName().getString())))));
        literalText.append("'s Ender chest").setStyle(new Style().setColor(Formatting.YELLOW));

        sender.sendChatMessage(literalText, MessageType.CHAT);
        EnderChestInventory enderChestInventory = target.getEnderChestInventory();
        sender.openContainer(new ClientDummyContainerProvider((i, pInv, pEntity) -> {
            return GenericContainer.createGeneric9x3(i, pInv, enderChestInventory);
        }, new TranslatableText("container.enderchest")));

        return 1;
    }

}
