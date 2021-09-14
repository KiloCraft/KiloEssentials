package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.util.TickManager;

public class TpsCommand extends EssentialCommand {
    public TpsCommand() {
        super("tps");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::run);
    }

    private int run(CommandContext<ServerCommandSource> ctx) {
        this.getCommandSource(ctx).sendMessage(String.format(
                "<gold>TPS %s <dark_gray>(<gray>%s ms<dark_gray>) <dark_gray>(<gray>1m<dark_gray>/<gray>5m<dark_gray>/<gray>15m<dark_gray>/<gray>1h<dark_gray>) %s<dark_gray>, %s<dark_gray>, %s<dark_gray>, %s<reset>",
                ComponentText.formatTps(TickManager.tps[0]),
                TickManager.getFormattedMSPT(),
                ComponentText.formatTps(TickManager.tps[2]),
                ComponentText.formatTps(TickManager.tps[3]),
                ComponentText.formatTps(TickManager.tps[4]),
                ComponentText.formatTps(TickManager.tps[5])));

        return (int) Math.floor(TickManager.tps[0]);
    }

}
