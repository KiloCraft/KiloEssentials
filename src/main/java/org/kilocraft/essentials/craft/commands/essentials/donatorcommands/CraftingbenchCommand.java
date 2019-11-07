package org.kilocraft.essentials.craft.commands.essentials.donatorcommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.container.BlockContext;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import org.kilocraft.essentials.craft.KiloCommands;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CraftingbenchCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("craftingbench");
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = literal("craftingbench")
                .requires(source -> Thimble.hasPermissionOrOp(source, KiloCommands.getCommandPermission("craftingbench"), 2))
                    .then(argument("player", player())
                            .executes(context -> execute(context, getPlayer(context, "player").getCommandSource(), true)))
                .executes(context -> execute(context, context.getSource(), false));

        dispatcher.register(literalArgumentBuilder);
        dispatcher.register(literal("craft").requires(source -> Thimble.hasPermissionOrOp(source, KiloCommands.getCommandPermission("craftingbench"), 2))
                .executes(context -> execute(context, context.getSource(), false)));
    }

    private static int execute(CommandContext<ServerCommandSource> context, ServerCommandSource source, boolean sendFeedback) throws CommandSyntaxException {
        LiteralText literalText = new LiteralText("");
        ServerPlayerEntity target = source.getPlayer();
        ServerPlayerEntity sender = context.getSource().getPlayer();
        literalText.append("You have opened the Crafting Bench for ").setStyle(new Style().setColor(Formatting.YELLOW));  // TODO Magic value
        literalText.append(new LiteralText(target.getName().getString()).setStyle(new Style().setColor(Formatting.GOLD)
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new LiteralText(target.getName().getString())))));

        if (sendFeedback) sender.sendChatMessage(literalText, MessageType.CHAT);

        target.openContainer(
                new ClientDummyContainerProvider((i, inv, player) -> {
                    return new CraftingTableContainer(i, inv, BlockContext.create(source.getWorld(), new BlockPos(0, 0, 0)));
                }, new TranslatableText("container.crafting"))
        );

        return 1;
    }
}
