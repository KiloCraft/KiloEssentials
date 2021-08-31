package org.kilocraft.essentials.util.commands.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PowerToolsCommand {
    private static Predicate<ServerCommandSource> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.ITEM_COMMANDS, 4);

    public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> builder) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("command")
                .requires(PERMISSION_CHECK)
                .executes(PowerToolsCommand::executeList)
                .build();

        LiteralArgumentBuilder<ServerCommandSource> resetArgument = literal("reset")
                .executes(PowerToolsCommand::executeReset);

        LiteralArgumentBuilder<ServerCommandSource> removeArgument = literal("remove");
        RequiredArgumentBuilder<ServerCommandSource, Integer> removeLineArgument = argument("line", integer(1, 10))
                .suggests(ArgumentSuggestions::noSuggestions)
                .executes(PowerToolsCommand::executeRemove);

        LiteralArgumentBuilder<ServerCommandSource> setArgument = literal("set");

        RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = argument("line", integer(1, 10))
                .suggests(ArgumentSuggestions::noSuggestions);

        RequiredArgumentBuilder<ServerCommandSource, String> textArgument = argument("command", greedyString())
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

    private static CompletableFuture<Suggestions> commandSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ItemStack item = context.getSource().getPlayer().getMainHandStack();

        if (item.isEmpty() || !item.hasNbt() || item.getNbt() == null || !item.getNbt().contains("NBTCommands"))
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

        NbtList commands = item.getNbt().getList("NBTCommands", 8);
        String[] strings = {commands.getString(inputLine)};
        return CommandSource.suggestMatching(strings, builder);
    }

    private static int executeRemove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        int inputLine = getInteger(ctx, "line") - 1;
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ItemStack item = player.getMainHandStack();
        CommandSourceUser user = new CommandSourceServerUser(ctx.getSource());

        if (item.isEmpty()) {
            user.sendLangMessage("command.item.invalid_item");
            return -1;
        }

        if (!item.hasNbt() || item.getNbt() == null || !item.getNbt().contains("NBTCommands")) {
            user.sendLangMessage("command.item.nothing_to_reset");
            return -1;
        }

        NbtList lore = item.getNbt().getList("NBTCommands", 8);

        if (inputLine >= lore.size()) {
            user.sendLangMessage("command.item.nothing_to_reset");
            return -1;
        }

        lore.remove(inputLine);

        user.sendLangMessage("command.item.command.remove", inputLine + 1);
        return 1;
    }

    private static int executeReset(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ItemStack item = ctx.getSource().getPlayer().getMainHandStack();
        CommandSourceUser user = new CommandSourceServerUser(ctx.getSource());

        if (item.isEmpty()) {
            user.sendLangMessage("command.item.invalid_item");
            return -1;
        }

        if (item.getNbt() == null) {
            user.sendLangMessage("command.item.nothing_to_reset");
            return -1;
        }

        Objects.requireNonNull(item.getNbt()).remove("NBTCommands");
        user.sendLangMessage("command.item.reset", "command", "not-set");
        return 1;
    }

    private static int executeList(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ItemStack item = player.getMainHandStack();
        CommandSourceUser user = new CommandSourceServerUser(ctx.getSource());

        if (item.isEmpty()) {

            user.sendLangMessage("command.item.no_item");
            return -1;
        }

        if (!item.hasNbt() || item.getNbt() == null || !item.getNbt().contains("NBTCommands")) {
            user.sendLangMessage("command.item.command.no_commands");
            return -1;
        }

        NbtList commands = item.getNbt().getList("NBTCommands", 8);

        MutableText text = new LiteralText("PowerTool Commands:").formatted(Formatting.GOLD);

        for (int i = 0; i < commands.size(); i++) {
            if (commands.getString(i).equals(""))
                continue;

            text.append(new LiteralText("\n - ").formatted(Formatting.YELLOW))
                    .append(new LiteralText(commands.getString(i)).formatted(Formatting.WHITE));
        }

        user.sendMessage(text);
        return 1;
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String inputString = getString(ctx, "command").replaceFirst("/", "");
        ItemStack item = player.getMainHandStack();
        CommandSourceUser user = new CommandSourceServerUser(ctx.getSource());

        if (item.isEmpty()) {
            user.sendLangMessage("command.item.invalid_item");
            return -1;
        }

        NbtCompound itemTag = item.getNbt();

        if (!item.hasNbt()) {
            itemTag = new NbtCompound();
        }

        if (!itemTag.contains("NBTCommands")) {
            itemTag.put("NBTCommands", new NbtCompound());
        }

        NbtList command = itemTag.getList("NBTCommands", 8);
        int inputLine = getInteger(ctx, "line") - 1;

        if (command == null) {
            command = new NbtList();
        }

        if (inputLine > command.size() - 1) {
            for (int i = command.size(); i <= inputLine; i++) {
                if (!command.getString(i).isEmpty())
                    continue;

                command.add(NbtString.of(inputString));
            }
        }

        command.set(inputLine, NbtString.of(inputString));
        itemTag.put("NBTCommands", command);
        item.setNbt(itemTag);

        user.sendLangMessage("command.item.set", "command", inputLine + 1, "&e:\n &7" + inputLine);
        return 1;
    }
}
