package org.kilocraft.essentials.commands.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.container.AnvilContainer;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.chat.KiloChat;

import static net.minecraft.server.command.CommandManager.literal;

public class AnvilCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = literal("anvil")
                .requires(source -> KiloCommands.hasPermission(source, CommandPermission.ANVIL))
                .executes(AnvilCommand::execute);

        dispatcher.register(literalArgumentBuilder);
    }

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        KiloChat.sendLangMessageTo(context.getSource(), "general.open_container", "Anvil");

        player.openContainer(new ClientDummyContainerProvider(AnvilCommand::createMenu, new TranslatableText("container.repair")));
        return 1;
    }

    private static Container createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new AnvilContainer(syncId, playerInventory);
    }
}
