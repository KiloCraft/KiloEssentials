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
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.util.nbt.NBTTypes;

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
				.suggests(ArgumentSuggestions::noSuggestions)
				.executes(ItemLoreCommand::executeRemove);

		LiteralArgumentBuilder<ServerCommandSource> setArgument = literal("set");

		RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = argument("line", integer(1, 10))
				.suggests(ArgumentSuggestions::noSuggestions);

		RequiredArgumentBuilder<ServerCommandSource, String> textArgument = argument("text", greedyString())
				.suggests(ArgumentSuggestions::noSuggestions)
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
        CommandSourceUser user = KiloServer.getServer().getCommandSourceUser(ctx.getSource());

		if (item.isEmpty()) {
			user.sendLangMessage( "general.no_item");
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

	private static int executeReset(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ItemStack item = ctx.getSource().getPlayer().getMainHandStack();
        CommandSourceUser user = KiloServer.getServer().getCommandSourceUser(ctx.getSource());

		if (item.isEmpty()) {
			user.sendLangMessage("general.no_item");
			return -1;
		}

		if (item.getTag() == null) {
			user.sendLangMessage("command.item.nothing_to_reset");
			return -1;
		}

		Objects.requireNonNull(item.getTag()).getCompound("display").remove("Lore");
		user.sendLangMessage("command.item.lore.reset");
		return 1;
	}

	private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayer();
		String inputString = getString(ctx, "text");
		ItemStack item = player.getMainHandStack();
        CommandSourceUser user = KiloServer.getServer().getCommandSourceUser(ctx.getSource());

		if (inputString.length() >= 90) {
			user.sendLangMessage("command.item.too_long");
			return -1;
		}

		if (item.isEmpty()) {
			user.sendLangMessage("general.no_item");
			return -1;
		}

		CompoundTag itemTag = item.getTag();

		AtomicBoolean containsEnchantmentName = new AtomicBoolean(false);
		Registry.ENCHANTMENT.forEach((enchantment) -> {
			if (ComponentText.clearFormatting(inputString).contains(enchantment.getName(1).getString())) {
				containsEnchantmentName.set(true);
			}
		});

		if (containsEnchantmentName.get()) {
			user.sendLangMessage( "command.item.contains_enchantment_name");
			return -1;
		}

		if (player.experienceLevel < 1 && !player.isCreative()) {
			user.sendLangMessage("command.item.no_exp");
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

        String text = KiloCommands.hasPermission(ctx.getSource(), CommandPermission.ITEM_FORMATTING) ? inputString : ComponentText.clearFormatting(inputString);

		lore.set(inputLine, StringTag.of(Text.Serializer.toJson(ComponentText.toText(text))));
		itemTag.getCompound("display").put("Lore", lore);
		item.setTag(itemTag);

		user.sendLangMessage("command.item.lore.set", inputLine + 1, text);
		return 1;
	}

}
