package org.kilocraft.essentials.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.Map.Entry;
import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.literal;

public class ItemMendCommand {
    private static Predicate<ServerCommandSource> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.ITEM_MEND, 2);

    public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> builder, CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("command")
                .requires(PERMISSION_CHECK)
                .executes(ItemMendCommand::execute)
                .build();

        builder.then(rootCommand);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        OnlineUser src = KiloServer.getServer().getOnlineUser(player);
        ItemStack stack = player.getMainHandStack();

        if (stack.isEmpty()) {
            src.sendLangError("command.item.invalid_item");
            return -1;
        }

        if (!stack.isDamaged()) {
            src.sendLangError("command.item.mend.not_damaged");
            return -1;
        }

        Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.chooseEquipmentWith(Enchantments.MENDING, player, ItemStack::isDamaged);
        if (entry == null) {
            src.sendLangError("command.item.mend.no_mending");
            return -1;
        }


        int min = Math.min(repairAmount(0), stack.getDamage());



        return 1;
    }

    private static int repairAmount(int i) {
        return i * 2;
    }

}
