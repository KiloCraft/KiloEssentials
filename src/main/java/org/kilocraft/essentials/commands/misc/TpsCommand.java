package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.util.TPSTracker;

import static org.kilocraft.essentials.util.TPSTracker.*;

public class TpsCommand extends EssentialCommand {
    public TpsCommand() {
        super("tps");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::run);
    }

    private int run(CommandContext<ServerCommandSource> ctx) {
        KiloChat.sendMessageToSource(ctx.getSource(), new TextMessage(String.format(
                "&6TPS&%s %s&7 &8(&7%s ms&8) &8(&75m&8/&715m&8/&730m&8/&71h&8)&%s %s&8,&%s %s&8,&%s %s&8,&%s %s&r",
                TextFormat.getFormattedTPS(tps1.getAverage()), tps1.getShortAverage(),
                TPSTracker.MillisecondPerTick.getShortAverage(),
                TextFormat.getFormattedTPS(tps5.getAverage()), tps5.getShortAverage(),
                TextFormat.getFormattedTPS(tps15.getAverage()), tps15.getShortAverage(),
                TextFormat.getFormattedTPS(tps30.getAverage()), tps30.getShortAverage(),
                TextFormat.getFormattedTPS(tps60.getAverage()), tps60.getShortAverage()), true));

        return (int) Math.floor(tps1.getAverage());
    }

}
