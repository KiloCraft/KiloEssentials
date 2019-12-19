package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.provided.BrandedServer;

import static net.minecraft.server.command.CommandManager.literal;

public class ReloadCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> reload = dispatcher.register(literal("ke_reload")
            .requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.server.manage.reload", 2))
            .executes(context -> execute(context.getSource()))
        );

        dispatcher.register(literal("rl")
                .requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.server.manage.reload", 2))
                .executes(context -> execute(context.getSource()))
        );
        dispatcher.getRoot().addChild(reload);
    }

    private static int execute(ServerCommandSource source) {
        KiloChat.sendLangMessageTo(source, "command.reload.start");

        KiloConfig.load();
        source.getMinecraftServer().reload();
        BrandedServer.provide();

        KiloChat.sendLangMessageTo(source, "command.reload.end");

        return 1;
    }
}
