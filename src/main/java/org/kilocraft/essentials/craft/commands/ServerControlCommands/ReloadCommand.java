package org.kilocraft.essentials.craft.commands.ServerControlCommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.craft.utils.LangText;

public class ReloadCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(CommandManager.literal("reload")
            .requires(source -> source.hasPermissionLevel(3))
                .then(CommandManager.literal("World").executes(c -> reloadWorld(c.getSource())))
                .then(CommandManager.literal("config").executes(c -> reloadConfig(c.getSource(), true)))
            .executes(c -> {
                reloadConfig(c.getSource(), false);
                return reloadWorld(c.getSource());
            })
        );

        dispatcher.register(CommandManager.literal("rl")
            .requires(source -> source.hasPermissionLevel(3))
            .executes(context -> {
                reloadConfig(context.getSource(), false);
                return reloadWorld(context.getSource());
            }));
    }

    private static int reloadConfig(ServerCommandSource source, boolean bool) {
        if (bool) source.sendFeedback(LangText.get(false, "commands.reload.config")
                            .setStyle(new Style().setColor(Formatting.GREEN)),
                    false);

        return 1;
    }

    private static int reloadWorld(ServerCommandSource source) {
        source.sendFeedback(LangText.get(false, "commands.reload.all.start")
                .setStyle(new Style().setColor(Formatting.RED)),
                false);

        source.getMinecraftServer().reload();
        source.sendFeedback(LangText.get(false, "commands.reload.all.end")
                .setStyle(new Style().setColor(Formatting.GREEN)),
                false);
        return 1;
    }
}
