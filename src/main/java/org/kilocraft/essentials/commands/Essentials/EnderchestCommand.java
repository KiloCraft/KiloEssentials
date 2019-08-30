package org.kilocraft.essentials.commands.Essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.container.GenericContainer;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class EnderchestCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(
                CommandManager.literal("enderchest")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> execute(context, EntityArgumentType.getPlayer(context, "player").getCommandSource())))
                        .executes(context -> execute(context, context.getSource()))
        );

        dispatcher.register(CommandManager.literal("ec").redirect(literalCommandNode));
    }

    private static int execute(CommandContext<ServerCommandSource> context, ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity target = source.getPlayer();

        EnderChestInventory enderChestInventory = target.getEnderChestInventory();
        target.openContainer(new ClientDummyContainerProvider((i, inv, pEntity) ->
                GenericContainer.createGeneric9x3(i, inv, enderChestInventory),
                new TranslatableText("container.enderchest")));
        return 1;
    }

}
