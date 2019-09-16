package org.kilocraft.essentials.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.util.SomeGlobals;

public class TpsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tps")
                .requires(source -> Thimble.hasPermissionOrOp(source, "kiloapi.command.tps", 2))
                .executes(TpsCommand::run)
        );
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        LiteralText literalText = new LiteralText("");

        literalText.append("TPS").formatted(Formatting.YELLOW);
        literalText.append("(1m/5m/15m)").formatted(Formatting.GRAY);
        literalText.append(": ").formatted(Formatting.YELLOW);
        literalText.append(String.format("%s.02f", SomeGlobals.tps1.getAverage())).formatted(Formatting.GOLD);
        literalText.append(String.format("%s.02f", SomeGlobals.tps5.getAverage())).formatted(Formatting.GOLD);
        literalText.append(String.format("%s.02f", SomeGlobals.tps15.getAverage())).formatted(Formatting.GOLD);

        context.getSource().sendFeedback(literalText, false);
        return 1;
    }
}
