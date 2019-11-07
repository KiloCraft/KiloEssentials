package org.kilocraft.essentials.craft.commands.servermanagement;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.chat.KiloChat;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.provider.KiloBrandName;

import static net.minecraft.server.command.CommandManager.literal;

public class ReloadCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(literal("ke_reload")
            .requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.server.manage.reload", 2))
            .executes(context -> execute(context.getSource()))
        );

        dispatcher.register(literal("rl")
                .requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.server.manage.reload", 2))
                .executes(context -> execute(context.getSource()))
        );
    }

    private static int execute(ServerCommandSource source) {
        KiloChat.sendLangMessageTo(source, "command.reload.start");

        KiloConifg.load();
        source.getMinecraftServer().reload();
        KiloBrandName.provide();

        KiloChat.sendLangMessageTo(source, "command.reload.end");

        return 1;
    }
}
