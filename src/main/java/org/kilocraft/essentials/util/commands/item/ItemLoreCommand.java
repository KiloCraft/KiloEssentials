package org.kilocraft.essentials.util.commands.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.nbt.NBTTypes;

import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ItemLoreCommand {
    private static final Predicate<CommandSourceStack> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.ITEM_LORE);

    public static void registerChild(LiteralArgumentBuilder<CommandSourceStack> builder) {
        LiteralCommandNode<CommandSourceStack> rootCommand = literal("lore")
                .requires(PERMISSION_CHECK)
                .build();

        LiteralArgumentBuilder<CommandSourceStack> resetArgument = literal("reset")
                .executes(ItemLoreCommand::executeReset);

        LiteralArgumentBuilder<CommandSourceStack> removeArgument = literal("remove");
        RequiredArgumentBuilder<CommandSourceStack, Integer> removeLineArgument = argument("line", integer(1, 10))
                .suggests(ArgumentSuggestions::noSuggestions)
                .executes(ItemLoreCommand::executeRemove);

        LiteralArgumentBuilder<CommandSourceStack> setArgument = literal("set");

        RequiredArgumentBuilder<CommandSourceStack, Integer> lineArgument = argument("line", integer(1, 10))
                .suggests(ArgumentSuggestions::noSuggestions);

        RequiredArgumentBuilder<CommandSourceStack, String> textArgument = argument("text", greedyString())
                .suggests(ArgumentSuggestions::noSuggestions)
                .executes(ItemLoreCommand::execute);

        removeArgument.then(removeLineArgument);
        lineArgument.then(textArgument.build());
        setArgument.then(lineArgument.build());
        rootCommand.addChild(removeArgument.build());
        rootCommand.addChild(resetArgument.build());
        rootCommand.addChild(setArgument.build());
        builder.then(rootCommand);
    }

    private static int executeRemove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int inputLine = getInteger(ctx, "line") - 1;
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack item = player.getMainHandItem();
        CommandSourceUser user = CommandSourceServerUser.of(ctx);

        if (item.isEmpty()) {
            user.sendLangMessage("command.item.no_item");
            return -1;
        }

        if (!item.hasTag() || item.getTag() == null || !item.getTag().contains("display") || !item.getTag().getCompound("display").contains("Lore")) {
            user.sendLangMessage("command.item.nothing_to_reset");
            return -1;
        }

        ListTag lore = item.getTag().getCompound("display").getList("Lore", NBTTypes.STRING);

        if (inputLine >= lore.size()) {
            user.sendLangMessage("command.item.nothing_to_reset");
            return -1;
        }

        lore.remove(inputLine);

        user.sendLangMessage("command.item.lore.remove", inputLine + 1);
        return 1;
    }

    private static int executeReset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack item = ctx.getSource().getPlayerOrException().getMainHandItem();
        CommandSourceUser user = CommandSourceServerUser.of(ctx);

        if (ModifyItemCommand.validate(user, item)) return -1;

        if (item.getTag() == null) {
            user.sendLangMessage("command.item.nothing_to_reset");
            return -1;
        }

        player.giveExperienceLevels(-1);

        item.getTag().getCompound("display").remove("Lore");
        user.sendLangMessage("command.item.lore.reset");
        return 1;
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String inputString = getString(ctx, "text");
        ItemStack item = player.getMainHandItem();
        CommandSourceUser user = CommandSourceServerUser.of(ctx);

        if (ModifyItemCommand.validate(user, item, inputString)) return -1;

        CompoundTag itemTag = item.getTag();

        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (ComponentText.clearFormatting(inputString).contains(enchantment.getFullname(1).getString())) {
                user.sendLangMessage("command.item.contains_enchantment_name");
                return -1;
            }
        }

        if (!item.hasTag()) {
            itemTag = new CompoundTag();
        }

        if (!itemTag.contains("display")) {
            itemTag.put("display", new CompoundTag());
        }

        ListTag lore = itemTag.getCompound("display").getList("Lore", 8);
        int inputLine = getInteger(ctx, "line") - 1;

        if (lore == null) {
            lore = new ListTag();
        }

        if (inputLine > lore.size() - 1) {
            for (int i = lore.size(); i <= inputLine; i++) {
                lore.add(StringTag.valueOf("{\"text\":\"\"}"));
            }
        }

        String text = KiloCommands.hasPermission(ctx.getSource(), CommandPermission.ITEM_FORMATTING) ? inputString : ComponentText.clearFormatting(inputString);

        player.giveExperienceLevels(-1);
        lore.set(inputLine, StringTag.valueOf(Component.Serializer.toJson(ComponentText.toText(text))));
        itemTag.getCompound("display").put("Lore", lore);
        item.setTag(itemTag);

        user.sendLangMessage("command.item.lore.set", inputLine + 1, text);
        return 1;
    }

}
