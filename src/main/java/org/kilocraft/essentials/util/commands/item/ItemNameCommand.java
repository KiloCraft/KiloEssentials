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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.text.Texter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ItemNameCommand {
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.ITEM_NAME);

    public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> builder) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("name")
                .requires(PERMISSION_CHECK).build();
        RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name", greedyString())
                .suggests(ItemNameCommand::itemNameSuggestions)
                .executes(ItemNameCommand::execute);

        rootCommand.addChild(nameArgument.build());
        builder.then(rootCommand);
    }

    private static CompletableFuture<Suggestions> itemNameSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ItemStack item = context.getSource().getPlayer().getMainHandStack();
        List<String> list = new ArrayList<String>() {{
            add("reset");
        }};
        if (!item.isEmpty()) {
            list.add(item.getName().asString());
        }
        return CommandSource.suggestMatching(list, builder);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String inputString = getString(ctx, "name");
        ItemStack item = player.getMainHandStack();
        CommandSourceUser user = new CommandSourceServerUser(ctx.getSource());

        if (ModifyItemCommand.validate(user, item, inputString)) return -1;


		player.addExperienceLevels(-1);

        if (inputString.equalsIgnoreCase("reset")) {
            item.removeCustomName();
            user.sendLangMessage( "command.item.reset", "name", Texter.Legacy.toFormattedString(item.getName()));
            return 1;
        }

        String nameToSet = KiloCommands.hasPermission(ctx.getSource(), CommandPermission.ITEM_FORMATTING) ? inputString : ComponentText.clearFormatting(inputString);
        user.sendLangMessage( "command.item.set", "name", nameToSet);
        item.setCustomName(ComponentText.toText(nameToSet));

        return 1;
    }

}
