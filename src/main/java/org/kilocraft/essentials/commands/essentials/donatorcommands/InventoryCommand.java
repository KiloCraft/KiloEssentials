package org.kilocraft.essentials.commands.essentials.donatorcommands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.container.Container;
import net.minecraft.container.GenericContainer;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.util.CommandHelper;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.craft.gui.GUI;

import java.util.Collection;
import java.util.Iterator;

public abstract class InventoryCommand {
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

    public static int openInventory(ServerPlayerEntity sender, ServerPlayerEntity target) {
//        Inventory gui = new Inventory() {
//            @Override
//            public int getInvSize() {
//                return 54;
//            }
//
//            @Override
//            public boolean isInvEmpty() {
//                return false;
//            }
//
//            @Override
//            public ItemStack getInvStack(int i) {
//                ItemStack itemStack = null;
//                for(int j = 0; j < 54; j++){
//                    itemStack = target.inventory.getInvStack(i);
//                }
//                return itemStack;
//            }
//
//            @Override
//            public ItemStack takeInvStack(int i, int i1) {
//                return null;
//            }
//
//            @Override
//            public ItemStack removeInvStack(int i) {
//                return null;
//            }
//
//            @Override
//            public void setInvStack(int i, ItemStack itemStack) {
//
//            }
//
//            @Override
//            public void markDirty() {
//
//            }
//
//            @Override
//            public boolean canPlayerUseInv(PlayerEntity playerEntity) {
//                return true;
//            }
//
//            @Override
//            public void clear() {
//
//            }
//        };
        GUI gui = new GUI(54);
        target.inventory.onInvOpen(sender);
        sender.openContainer(new NameableContainerProvider() {
            @Override
            public LiteralText getDisplayName() {
                return (LiteralText) target.getName();
            }

            @Override
            public Container createMenu(int id, PlayerInventory inventory, PlayerEntity var3) {
//                return GenericContainer.createGeneric9x6(id, inventory, new DoubleInventory(target.inventory, target.getEnderChestInventory()));
                return GenericContainer.createGeneric9x6(id, inventory, gui);
            }
        });
        return 1;
    }

}