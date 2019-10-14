package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextColor;
import org.kilocraft.essentials.api.util.CommandSuggestions;

public class SuicideCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("suicide")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.suicide.self", 2))
                .executes(c -> execute(c.getSource(), c.getSource().getPlayer()));

        argumentBuilder.then(
                CommandManager.argument("target", EntityArgumentType.player())
                    .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.suicide.others", 2))
                    .suggests((context, builder) -> {
                        return CommandSuggestions.allPlayers.getSuggestions(context, builder);
                    })
                    .executes(c -> execute(c.getSource(), EntityArgumentType.getPlayer(c, "target")))
        );

        dispatcher.register(argumentBuilder);
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity target) {
        target.setHealth(-1F);
        Text text = new LiteralText(target.getName().asString() + " have ended their life...").formatted(Formatting.ITALIC, Formatting.GRAY);
        KiloServer.getServer().getPlayerManager().sendToAll(text);
        if (!source.getName().equals(target.getName().asString()))
            TextColor.sendToUniversalSource(source, "Suicided " + target.getName(), true);
        return 1;
    }
}
