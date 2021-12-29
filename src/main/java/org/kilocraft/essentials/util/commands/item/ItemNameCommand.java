package org.kilocraft.essentials.util.commands.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.text.Texter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ItemNameCommand {
    private static final Predicate<CommandSourceStack> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.ITEM_NAME);

    public static void registerChild(LiteralArgumentBuilder<CommandSourceStack> builder) {
        LiteralCommandNode<CommandSourceStack> rootCommand = literal("name")
                .requires(PERMISSION_CHECK).build();
        RequiredArgumentBuilder<CommandSourceStack, String> nameArgument = argument("name", greedyString())
                .suggests(ItemNameCommand::itemNameSuggestions)
                .executes(ItemNameCommand::execute);

        rootCommand.addChild(nameArgument.build());
        builder.then(rootCommand);
    }

    private static CompletableFuture<Suggestions> itemNameSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ItemStack item = context.getSource().getPlayerOrException().getMainHandItem();
        List<String> list = new ArrayList<String>() {{
            this.add("reset");
        }};
        if (!item.isEmpty()) {
            list.add(item.getHoverName().getContents());
        }
        return SharedSuggestionProvider.suggest(list, builder);
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String inputString = getString(ctx, "name");
        ItemStack item = player.getMainHandItem();
        CommandSourceUser user = CommandSourceServerUser.of(ctx);

        if (ModifyItemCommand.validate(user, item, inputString)) return -1;


        player.giveExperienceLevels(-1);

        if (inputString.equalsIgnoreCase("reset")) {
            item.resetHoverName();
            user.sendLangMessage("command.item.reset", "name", Texter.Legacy.toFormattedString(item.getHoverName()));
            return 1;
        }

        String nameToSet = KiloCommands.hasPermission(ctx.getSource(), CommandPermission.ITEM_FORMATTING) ? inputString : ComponentText.clearFormatting(inputString);
        user.sendLangMessage("command.item.set", "name", nameToSet);
        item.setHoverName(ComponentText.toText(nameToSet));

        return 1;
    }

}
