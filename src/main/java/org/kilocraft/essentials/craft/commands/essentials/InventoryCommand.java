package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.container.Container;
import net.minecraft.container.Generic3x3Container;
import net.minecraft.container.GenericContainer;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DefaultedList;
import net.minecraft.world.IWorld;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.util.CommandHelper;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;

import java.util.Collection;
import java.util.Iterator;

public class InventoryCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("inventory");
        KiloCommands.getCommandPermission("inventory.others");
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("inventory").requires(InventoryCommand::permission)
                .executes(c -> openInventory(c.getSource().getPlayer(), c.getSource().getPlayer()));
        LiteralArgumentBuilder<ServerCommandSource> aliasBuilder = CommandManager.literal("inv").requires(InventoryCommand::permission)
                .executes(c -> openInventory(c.getSource().getPlayer(), c.getSource().getPlayer()));;

        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> selectorArg = CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("inventory.others"), 2))
                .suggests((context, builder) -> {
                    return CommandSuggestions.allPlayers.getSuggestions(context, builder);
                })
                .executes(context -> execute(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "gameProfile")));

        argumentBuilder.then(selectorArg);
        aliasBuilder.then(selectorArg);

        dispatcher.register(aliasBuilder);
        dispatcher.register(argumentBuilder);
    }

    private static boolean permission(ServerCommandSource source) {
        return Thimble.hasPermissionOrOp(source, "kiloessentials.command.inventory", 2);
    }

    private static int execute(ServerCommandSource source, Collection<GameProfile> gameProfiles) throws CommandSyntaxException {
        Iterator v = gameProfiles.iterator();

        if (gameProfiles.size() > 1) source.sendError(new LiteralText("You can only select one player but the provided selector includes more!"));
        else if (!CommandHelper.isConsole(source)) {
            GameProfile gameProfile = (GameProfile) v.next();
            ServerPlayerEntity ecSource = KiloServer.getServer().getPlayerManager().getPlayer(gameProfile.getId());

            TextFormat.sendToSource(source, false, "&eNow looking at &6%s's&e inventory", gameProfile.getName());

            openInventory(source.getPlayer(), ecSource);
        }
        else source.sendError(new LiteralText("Only players can use this command!"));

        return 1;
    }

    public static int openInventory(ServerPlayerEntity sender, ServerPlayerEntity targetInventory) {
        PlayerInventory inventory = new PlayerInventory(new PlayerEntity(targetInventory.getServerWorld(), targetInventory.getGameProfile()) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        });

        for(int i = 0; i < 26; i++){
            sender.sendMessage(new LiteralText(targetInventory.inventory.main.get(i).toString()));
            inventory.main.set(i, targetInventory.inventory.main.get(i));
        }

//        NameableContainerProvider nameableContainerProvider_1 = ChestBlock.createContainerProvider(blockState_1, world_1, blockPos_1);

        sender.openContainer(new ClientDummyContainerProvider((i, pInv, pEntity) -> {
            return GenericContainer.createGeneric9x4(i, inventory);
        }, new TranslatableText("container.inventory")));

//        sender.openContainer(new ClientDummyContainerProvider((i, pInv, pEntity) -> {
//            return GenericContainer.createGeneric9x3(i, inventory);
//        }, new TranslatableText("container.inventory")));

//        sender.openContainer(new ClientDummyContainerProvider((i, pInv, pEntity) -> {
//            return GenericContainer.createGeneric9x6(i, playerInventory);
//        }, new TranslatableText("container.inventory")));

        return 1;
    }

}