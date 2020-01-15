package org.kilocraft.essentials.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ItemLoreCommand {
	private static Predicate<ServerCommandSource> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.ITEM_LORE);

	public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> builder, CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> rootCommand = literal("lore")
				.requires(PERMISSION_CHECK)
				.build();

		LiteralArgumentBuilder<ServerCommandSource> resetArgument = literal("reset")
				.executes(ItemLoreCommand::executeReset);

		LiteralArgumentBuilder<ServerCommandSource> removeArgument = literal("remove");
		RequiredArgumentBuilder<ServerCommandSource, Integer> removeLineArgument = argument("line", integer(1, 10))
				.suggests(TabCompletions::noSuggestions)
				.executes(ItemLoreCommand::executeRemove);

		LiteralArgumentBuilder<ServerCommandSource> setArgument = literal("set");

		RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = argument("line", integer(1, 10))
				.suggests(TabCompletions::noSuggestions);

		RequiredArgumentBuilder<ServerCommandSource, String> textArgument = argument("text", greedyString())
				.suggests(TabCompletions::noSuggestions)
				.executes(ItemLoreCommand::execute);

		removeArgument.then(removeLineArgument);
		lineArgument.then(textArgument.build());
		setArgument.then(lineArgument.build());
		rootCommand.addChild(removeArgument.build());
		rootCommand.addChild(resetArgument.build());
		rootCommand.addChild(setArgument.build());
		builder.then(rootCommand);
		dispatcher.register(literal("relore").requires(PERMISSION_CHECK).redirect(rootCommand));
	}

	private static int executeRemove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		int inputLine = getInteger(ctx, "line") - 1;
		ServerPlayerEntity player = ctx.getSource().getPlayer();
		ItemStack item = player.getMainHandStack();

		if (item.isEmpty()) {
			KiloChat.sendLangMessageTo(ctx.getSource(), "command.item.invalid_item");
			return -1;
		}

		if (!item.hasTag() || item.getTag() == null || !item.getTag().contains("display") || !item.getTag().getCompound("display").contains("Lore")) {

		}

		ListTag lore = item.getTag().getCompound("display").getList("Lore", 8);

		if (inputLine >= lore.size()) {
			KiloChat.sendLangMessageTo(player, "command.item.nothing_to_reset");
			return -1;
		}

		lore.remove(inputLine);

		KiloChat.sendLangMessageTo(player, "command.item.lore.remove", inputLine + 1);
		return 1;
	}

	private static int executeReset(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ItemStack item = ctx.getSource().getPlayer().getMainHandStack();

		if (item.isEmpty()) {
			KiloChat.sendLangMessageTo(ctx.getSource(), "command.item.invalid_item");
			return -1;
		}

		if (item.getTag() == null) {
			KiloChat.sendLangMessageTo(ctx.getSource(), "command.item.nothing_to_reset");
			return -1;
		}

		Objects.requireNonNull(item.getTag()).getCompound("display").remove("Lore");
		KiloChat.sendLangMessageTo(ctx.getSource(), "command.item.lore.reset");
		return 1;
	}

	private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayer();
		String inputString = getString(ctx, "text");
		ItemStack item = player.getMainHandStack();

		if (inputString.length() >= 90) {
			KiloChat.sendLangMessageTo(player, "command.item.too_long");
			return -1;
		}

		if (item.isEmpty()) {
			KiloChat.sendLangMessageTo(player, "command.item.invalid_item");
			return -1;
		}

		CompoundTag itemTag = item.getTag();

		AtomicBoolean containsEnchantmentName = new AtomicBoolean(false);
		Registry.ENCHANTMENT.forEach((enchantment) -> {
			if (TextFormat.translate(inputString, false).contains(enchantment.getName(1).asString()))
				containsEnchantmentName.set(true);
		});

		if (containsEnchantmentName.get()) {
			KiloChat.sendLangMessageTo(player, "command.item.contains_enchantment_name");
			return -1;
		}

		if (player.experienceLevel < 1 && !player.isCreative()) {
			KiloChat.sendLangMessageTo(player, "command.item.no_exp");
			return 0;
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
				lore.add(StringTag.of("{\"text\":\"\"}"));
			}
		}

		String text = TextFormat.translate(inputString,
				KiloCommands.hasPermission(ctx.getSource(), CommandPermission.ITEM_FORMATTING));

		lore.set(inputLine, StringTag.of("{\"text\":\"" + text + "\"}"));
		itemTag.getCompound("display").put("Lore", lore);
		item.setTag(itemTag);

		KiloChat.sendLangMessageTo(player, "command.item.lore.set", inputLine + 1, text);
		return 1;
	}

}
