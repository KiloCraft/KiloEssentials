package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.chat.MutableTextMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.util.TpsTracker;

import static org.kilocraft.essentials.util.TpsTracker.*;

public class TpsCommand extends EssentialCommand {
    public TpsCommand() {
        super("tps");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::run);
    }

    private int run(CommandContext<ServerCommandSource> ctx) {
        KiloChat.sendMessageToSource(ctx.getSource(), new MutableTextMessage(String.format(
                "&6TPS&%s %s&7 &8(&7%s ms&8) &8(&75m&8/&715m&8/&71h&8/&71d&8)&%s %s&8,&%s %s&8,&%s %s&8,&%s %s&r",
                TextFormat.getFormattedTPS(tps.getAverage()), tps.getShortAverage(),
                TpsTracker.MillisecondPerTick.getShortAverage(),
                TextFormat.getFormattedTPS(tps5.getAverage()), tps5.getShortAverage(),
                TextFormat.getFormattedTPS(tps15.getAverage()), tps15.getShortAverage(),
                TextFormat.getFormattedTPS(tps60.getAverage()), tps60.getShortAverage(),
                TextFormat.getFormattedTPS(tps1440.getAverage()), tps1440.getShortAverage()), true));

        return (int) Math.floor(tps.getAverage());
    }

}
