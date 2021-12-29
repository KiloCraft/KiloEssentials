package org.kilocraft.essentials.util.commands.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PowerToolsCommand {
    private static final Predicate<CommandSourceStack> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.ITEM_COMMANDS, 4);

    public static void registerChild(LiteralArgumentBuilder<CommandSourceStack> builder) {
        LiteralCommandNode<CommandSourceStack> rootCommand = literal("command")
                .requires(PERMISSION_CHECK)
                .executes(PowerToolsCommand::executeList)
                .build();

        LiteralArgumentBuilder<CommandSourceStack> resetArgument = literal("reset")
                .executes(PowerToolsCommand::executeReset);

        LiteralArgumentBuilder<CommandSourceStack> removeArgument = literal("remove");
        RequiredArgumentBuilder<CommandSourceStack, Integer> removeLineArgument = argument("line", integer(1, 10))
                .suggests(ArgumentSuggestions::noSuggestions)
                .executes(PowerToolsCommand::executeRemove);

        LiteralArgumentBuilder<CommandSourceStack> setArgument = literal("set");

        RequiredArgumentBuilder<CommandSourceStack, Integer> lineArgument = argument("line", integer(1, 10))
                .suggests(ArgumentSuggestions::noSuggestions);

        RequiredArgumentBuilder<CommandSourceStack, String> textArgument = argument("command", greedyString())
                .suggests(PowerToolsCommand::commandSuggestions)
                .executes(PowerToolsCommand::execute);

        removeArgument.then(removeLineArgument);
        lineArgument.then(textArgument.build());
        setArgument.then(lineArgument.build());
        rootCommand.addChild(removeArgument.build());
        rootCommand.addChild(resetArgument.build());
        rootCommand.addChild(setArgument.build());
        builder.then(rootCommand);
    }

    private static CompletableFuture<Suggestions> commandSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ItemStack item = context.getSource().getPlayerOrException().getMainHandItem();

        if (item.isEmpty() || !item.hasTag() || item.getTag() == null || !item.getTag().contains("NBTCommands"))
            return ArgumentSuggestions.noSuggestions(context, builder);

        int inputLine = 0;

        char[] chars = context.getInput().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            try {
                inputLine = Integer.parseInt(String.valueOf(chars[i])) - 1;
                if (chars[i++] == 0) inputLine = chars[i] + '0';
            } catch (NumberFormatException ignored) {
            }
        }

        ListTag commands = item.getTag().getList("NBTCommands", 8);
        String[] strings = {commands.getString(inputLine)};
        return SharedSuggestionProvider.suggest(strings, builder);
    }

    private static int executeRemove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int inputLine = getInteger(ctx, "line") - 1;
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack item = player.getMainHandItem();
        CommandSourceUser user = CommandSourceServerUser.of(ctx);

        if (item.isEmpty()) {
            user.sendLangMessage("command.item.invalid_item");
            return -1;
        }

        if (!item.hasTag() || item.getTag() == null || !item.getTag().contains("NBTCommands")) {
            user.sendLangMessage("command.item.nothing_to_reset");
            return -1;
        }

        ListTag lore = item.getTag().getList("NBTCommands", 8);

        if (inputLine >= lore.size()) {
            user.sendLangMessage("command.item.nothing_to_reset");
            return -1;
        }

        lore.remove(inputLine);

        user.sendLangMessage("command.item.command.remove", inputLine + 1);
        return 1;
    }

    private static int executeReset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ItemStack item = ctx.getSource().getPlayerOrException().getMainHandItem();
        CommandSourceUser user = CommandSourceServerUser.of(ctx);

        if (item.isEmpty()) {
            user.sendLangMessage("command.item.invalid_item");
            return -1;
        }

        if (item.getTag() == null) {
            user.sendLangMessage("command.item.nothing_to_reset");
            return -1;
        }

        Objects.requireNonNull(item.getTag()).remove("NBTCommands");
        user.sendLangMessage("command.item.reset", "command", "not-set");
        return 1;
    }

    private static int executeList(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack item = player.getMainHandItem();
        CommandSourceUser user = CommandSourceServerUser.of(ctx);

        if (item.isEmpty()) {

            user.sendLangMessage("command.item.no_item");
            return -1;
        }

        if (!item.hasTag() || item.getTag() == null || !item.getTag().contains("NBTCommands")) {
            user.sendLangMessage("command.item.command.no_commands");
            return -1;
        }

        ListTag commands = item.getTag().getList("NBTCommands", 8);

        MutableComponent text = new TextComponent("PowerTool Commands:").withStyle(ChatFormatting.GOLD);

        for (int i = 0; i < commands.size(); i++) {
            if (commands.getString(i).equals(""))
                continue;

            text.append(new TextComponent("\n - ").withStyle(ChatFormatting.YELLOW))
                    .append(new TextComponent(commands.getString(i)).withStyle(ChatFormatting.WHITE));
        }

        user.sendMessage(text);
        return 1;
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String inputString = getString(ctx, "command").replaceFirst("/", "");
        ItemStack item = player.getMainHandItem();
        CommandSourceUser user = CommandSourceServerUser.of(ctx);

        if (item.isEmpty()) {
            user.sendLangMessage("command.item.invalid_item");
            return -1;
        }

        CompoundTag itemTag = item.getTag();

        if (!item.hasTag()) {
            itemTag = new CompoundTag();
        }

        if (!itemTag.contains("NBTCommands")) {
            itemTag.put("NBTCommands", new CompoundTag());
        }

        ListTag command = itemTag.getList("NBTCommands", 8);
        int inputLine = getInteger(ctx, "line") - 1;

        if (command == null) {
            command = new ListTag();
        }

        if (inputLine > command.size() - 1) {
            for (int i = command.size(); i <= inputLine; i++) {
                if (!command.getString(i).isEmpty())
                    continue;

                command.add(StringTag.valueOf(inputString));
            }
        }

        command.set(inputLine, StringTag.valueOf(inputString));
        itemTag.put("NBTCommands", command);
        item.setTag(itemTag);

        user.sendLangMessage("command.item.set", "command", inputLine + 1, "&e:\n &7" + inputLine);
        return 1;
    }
}
