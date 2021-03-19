package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.util.math.DataTracker;

public class TpsCommand extends EssentialCommand {
    public TpsCommand() {
        super("tps");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::run);
    }

    private int run(CommandContext<ServerCommandSource> ctx) {
        getCommandSource(ctx).sendMessage(String.format(
                "<gold>TPS %s <dark_gray>(<gray>%s ms<dark_gray>) <dark_gray>(<gray>5m<dark_gray>/<gray>15m<dark_gray>/<gray>1h<dark_gray>/<gray>1d<dark_gray>) %s<dark_gray>, %s<dark_gray>, %s<dark_gray>, %s<reset>",
                ComponentText.formatTps(DataTracker.tps.getAverage(100)),
                DataTracker.getFormattedMSPT(),
                ComponentText.formatTps(DataTracker.tps.getAverage(6000)),
                ComponentText.formatTps(DataTracker.tps.getAverage(18000)),
                ComponentText.formatTps(DataTracker.tps.getAverage(72000)),
                ComponentText.formatTps(DataTracker.tps.getAverage(1728000))));

        return (int) Math.floor(DataTracker.tps.getAverage(100));
    }

}
