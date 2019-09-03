package org.kilocraft.essentials.commands.Essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloEssentials;
import org.kilocraft.essentials.mysql.home;
import org.kilocraft.essentials.utils.LangText;

import java.sql.SQLException;

public class HomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(
                CommandManager.literal("home")
                        .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    try {
                        return execute(context, context.getSource());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return 0;
                })
        );
    }
    private static int execute(CommandContext<ServerCommandSource> context, ServerCommandSource source) throws CommandSyntaxException, SQLException {
        KiloEssentials.getLogger.info("execute executed");
        try {
            home.setHome(source.getPlayer(),"test",source.getPlayer().getEntityWorld());
        } catch (Exception e) {
            KiloEssentials.getLogger.error(e);
        }
        return 1;
    }

}
