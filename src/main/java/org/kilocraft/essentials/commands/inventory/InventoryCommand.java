package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.EntitySelector;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.GenericContainer;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.function.Predicate;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class InventoryCommand {
    private static Predicate<ServerCommandSource> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.SEEK_INVENTORY);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("inv")
                .requires(PERMISSION_CHECK)
                .build();

        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("target", player())
                .suggests(TabCompletions::allPlayers)
                .executes(InventoryCommand::execute);

        rootCommand.addChild(selectorArgument.build());
        dispatcher.getRoot().addChild(rootCommand);
        dispatcher.register(literal("inventory").requires(PERMISSION_CHECK).redirect(rootCommand));
        dispatcher.register(literal("seekinv").requires(PERMISSION_CHECK).redirect(rootCommand));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity source = ctx.getSource().getPlayer();
        ServerPlayerEntity target = getPlayer(ctx, "target");

        target.inventory.onInvOpen(source);

        NameableContainerProvider container = new NameableContainerProvider() {
            @Override
            public Text getDisplayName() {
                String displayName = KiloServer.getServer().getOnlineUser(target).getDisplayname();
                return new LiteralText(TextFormat.translate(displayName + "&r's Inventory", false));
            }

            @Override
            public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return new GenericContainer(ContainerType.GENERIC_9X4, i, source.inventory, target.inventory, 4);
            }

        };

        KiloChat.sendLangMessageTo(source, "general.seek_container", target.getEntityName(), "Inventory");
        source.openContainer(container);

        return 1;
    }

}
