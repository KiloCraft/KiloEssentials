package org.kilocraft.essentials.craft.commands.servermanagement;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.chat.LangText;

public class ReloadCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(CommandManager.literal("ke_reload")
            .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.server.manage.reload", 2))
                .then(CommandManager.literal("world").executes(c -> reloadWorld(c.getSource())))
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
        if (bool) source.sendFeedback(LangText.get(false, "command.reload.config")
                            .setStyle(new Style().setColor(Formatting.GREEN)),
                    false);

        return 1;
    }

    private static int reloadWorld(ServerCommandSource source) {
        source.sendFeedback(LangText.get(false, "command.reload.all.start")
                .setStyle(new Style().setColor(Formatting.RED)),
                false);

        source.getMinecraftServer().reload();
        source.sendFeedback(LangText.get(false, "command.reload.all.end")
                .setStyle(new Style().setColor(Formatting.GREEN)),
                false);
        return 1;
    }
}
