package org.kilocraft.essentials.commands.Essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.container.AnvilContainer;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class AnvilCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("craftingbench")
                .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("target", EntityArgumentType.player())
                            .executes(context -> execute(context, EntityArgumentType.getPlayer(context, "target").getCommandSource(), true)))
                .executes(context -> execute(context, context.getSource(), false));

        dispatcher.register(literalArgumentBuilder);
        dispatcher.register(CommandManager.literal("anvil").requires(source -> source.hasPermissionLevel(2))
                .executes(context -> execute(context, context.getSource(), false)));
    }

    private static int execute(CommandContext<ServerCommandSource> context, ServerCommandSource source, boolean sendFeedback) throws CommandSyntaxException {
        LiteralText literalText = new LiteralText("");
        ServerPlayerEntity target = source.getPlayer();
        ServerPlayerEntity sender = context.getSource().getPlayer();
        literalText.append("You have opened the Anvil for ").setStyle(new Style().setColor(Formatting.YELLOW));
        literalText.append(new LiteralText(target.getName().getString()).setStyle(new Style().setColor(Formatting.GOLD)
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new LiteralText(target.getName().getString())))));

        if (sendFeedback) sender.sendChatMessage(literalText, MessageType.CHAT);

        target.openContainer(new ClientDummyContainerProvider((i, inv, pEntity) -> {
            return new AnvilContainer(i, inv);
        }, new TranslatableText("container.anvil")));

        return 1;
    }
}
