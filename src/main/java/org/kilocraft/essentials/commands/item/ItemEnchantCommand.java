package org.kilocraft.essentials.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.arguments.ItemEnchantmentArgumentType.getEnchantment;
import static net.minecraft.command.arguments.ItemEnchantmentArgumentType.itemEnchantment;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ItemEnchantCommand {
    private static Predicate<ServerCommandSource> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.ITEM_ENCHANT);

    public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> builder, CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("enchants")
                .requires(PERMISSION_CHECK)
                .build();

        LiteralArgumentBuilder<ServerCommandSource> resetArgument = literal("reset")
                .executes(ItemEnchantCommand::resetEnchantments);

        LiteralArgumentBuilder<ServerCommandSource> removeArgument = literal("remove");
        RequiredArgumentBuilder<ServerCommandSource, Enchantment> removeEnchantArgument = argument("enchantment", itemEnchantment())
                .suggests(ItemEnchantCommand::suggestItemEnchantments)
                .executes(ItemEnchantCommand::removeEnchantment);

        LiteralArgumentBuilder<ServerCommandSource> addArgument = literal("add");

        RequiredArgumentBuilder<ServerCommandSource, Enchantment> addEnchantArgument = argument("enchantment", itemEnchantment())
                .suggests(ItemEnchantCommand::enchantmentSuggestions)
                .executes(ctx -> addEnchantment(ctx, -1));

        RequiredArgumentBuilder<ServerCommandSource, Integer> levelArgument = argument("level", integer())
                .suggests(ArgumentSuggestions::noSuggestions)
                .executes(ctx -> addEnchantment(ctx, getInteger(ctx, "level")));

        removeArgument.then(removeEnchantArgument);
        addEnchantArgument.then(levelArgument);
        addArgument.then(addEnchantArgument.build());
        rootCommand.addChild(removeArgument.build());
        rootCommand.addChild(resetArgument.build());
        rootCommand.addChild(addArgument.build());
        dispatcher.getRoot().addChild(rootCommand);
        builder.then(literal("enchants").requires(PERMISSION_CHECK).redirect(rootCommand));
    }

    private static CompletableFuture<Suggestions> enchantmentSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        for (Identifier id : Registry.ENCHANTMENT.getIds()) strings.add(id.getPath());
        return CommandSource.suggestMatching(strings, builder);
    }

    private static CompletableFuture<Suggestions> suggestItemEnchantments(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> strings = new ArrayList<>();
        ItemStack itemStack = context.getSource().getPlayer().getMainHandStack();

        if (itemStack != null && itemStack.hasTag() && itemStack.getTag() != null && itemStack.getTag().contains("Enchantments")) {
            ListTag listTag = itemStack.getTag().getList("Enchantments", 10);

            for (int i = 0; i < enchantments; i++) {
                CompoundTag tag = listTag.getCompound(i);

                if (tag == null)
                    break;

                strings.add(new Identifier(tag.getString("id")).getPath());
            }

            return CommandSource.suggestMatching(strings, builder);
        }

        return enchantmentSuggestions(context, builder);
    }

    private static int enchantments = Registry.ENCHANTMENT.getIds().size();

    private static int addEnchantment(CommandContext<ServerCommandSource> ctx, int level) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        Enchantment enchantment = getEnchantment(ctx, "enchantment");
        ItemStack itemStack = source.getPlayer().getMainHandStack();

        if (itemStack.isEmpty()) {
            KiloChat.sendLangMessageTo(ctx.getSource(), "general.no_item");
            return -1;
        }

        int finalLevel = level == -1 ? enchantment.getMinLevel() : level;
        itemStack.addEnchantment(enchantment, finalLevel);

        KiloChat.sendLangMessageTo(source, "command.item.enchant.add", getEnchantmentName(enchantment), finalLevel);
        return 1;
    }

    private static int resetEnchantments(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ItemStack item = ctx.getSource().getPlayer().getMainHandStack();

        if (item.isEmpty()) {
            KiloChat.sendLangMessageTo(ctx.getSource(), "general.no_item");
            return -1;
        }

        if (item.getTag() == null) {
            KiloChat.sendLangMessageTo(ctx.getSource(), "command.item.nothing_to_reset");
            return -1;
        }

        Objects.requireNonNull(item.getTag()).remove("Enchantments");
        KiloChat.sendLangMessageTo(ctx.getSource(), "command.item.enchant.reset");
        return 1;
    }

    private static int removeEnchantment(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        Enchantment enchantment = getEnchantment(ctx, "enchantment");
        ItemStack itemStack = source.getPlayer().getMainHandStack();

        if (itemStack.isEmpty()) {
            KiloChat.sendLangMessageTo(ctx.getSource(), "general.no_item");
            return -1;
        }

        if (itemStack.getTag() == null || !itemStack.getTag().contains("Enchantments")) {
            KiloChat.sendLangMessageTo(source, "command.item.nothing_to_reset");
            return -1;
        }

        ListTag listTag = itemStack.getTag().getList("Enchantments", 10);

        int removedEnchantments = 0;
        for (int i = 0; i < enchantments; i++) {
            CompoundTag tag = listTag.getCompound(i);

            if (tag == null)
                break;

            if (tag.getString("id").equals(Objects.requireNonNull(Registry.ENCHANTMENT.getId(enchantment)).toString())) {
                listTag.remove(i);
                removedEnchantments++;
                i--;
            }
        }

        if (removedEnchantments == 0) {
            KiloChat.sendLangMessageTo(source, "command.item.nothing_to_reset");
            return -1;
        }

        KiloChat.sendLangMessageTo(source, "command.item.enchant.remove", getEnchantmentName(enchantment));
        return 1;
    }

    private static String getEnchantmentName(Enchantment enchantment) {
        String s = Objects.requireNonNull(Registry.ENCHANTMENT.getId(enchantment)).getPath();
        return s.replaceFirst(String.valueOf(s.charAt(0)), String.valueOf(s.charAt(0)).toUpperCase()).replaceAll("_", "");
    }

}
