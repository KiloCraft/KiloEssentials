package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.world.MonitorableWorld;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.monitor.SystemMonitor;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import static org.kilocraft.essentials.api.text.TextFormat.getFormattedTPS;
import static org.kilocraft.essentials.util.TPSTracker.*;

public class StatusCommand extends EssentialCommand {
    public StatusCommand() {
        super("status", CommandPermission.STATUS);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private static OperatingSystemMXBean bean = SystemMonitor.getOsSystemMXBean();

    private int execute(CommandContext<ServerCommandSource> ctx) {
        try {
            KiloChat.sendMessageToSource(ctx.getSource(), new TextMessage(getInfo(), true));
        } catch (Exception e) {
            String msg = "An unexpected exception occurred when processing the cpu usage" +
                    "Please report this to a Administrator";
            KiloChat.sendMessageTo(ctx.getSource(),
                    new LiteralText(msg + "\n Exception message:")
                            .append(new LiteralText(e.getMessage()).formatted(Formatting.WHITE)).formatted(Formatting.RED));

            KiloEssentials.getLogger().error(msg);
            e.printStackTrace();
        }

        return 1;
    }

    private static String getInfo() throws Exception {
        double ramUsage = SystemMonitor.getRamUsedPercentage();
        double cpuUsage = SystemMonitor.getCpuLoadPercentage();
        return "&eGeneral status:&r\n" +
                "&7Platform: &6" + bean.getArch() + " &d" + System.getProperty("os.name") +
                "\n&7Server uptime: &6" + TimeDifferenceUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime()) +
                "\n&7TPS:" +
                String.format("&%s %s&8,&8(&75m&8/&715m&8/&730m&8&8/&71h&8)&%s %s&8,&%s %s&8,&%s %s&8,&%s %s&r",
                        getFormattedTPS(tps1.getAverage()), tps1.getShortAverage(), getFormattedTPS(tps5.getAverage()), tps5.getShortAverage(),
                        getFormattedTPS(tps15.getAverage()), tps15.getShortAverage(), getFormattedTPS(tps30.getAverage()), tps30.getShortAverage(),
                        getFormattedTPS(tps60.getAverage()), tps60.getShortAverage()) +
                "\n&7CPU &8(&e" + SystemMonitor.systemMXBean.getAvailableProcessors() + "&8)&7:" +
                " &" + TextFormat.getFormattedPercentage(cpuUsage, true) + cpuUsage + "% Usage" +
                " &3" + Thread.activeCount() + " Running Threads" +
                "\n&7Memory &8(&e" + SystemMonitor.getRamMaxMB() + " max&8)&7: &" +
                TextFormat.getFormattedPercentage(ramUsage, true) + ramUsage + "% " +
                "&8(&b" + SystemMonitor.getRamUsedMB() + " MB" + "&8/&7" +
                SystemMonitor.getRamTotalMB() + " MB" + "&8)" +
                "\n&7Worlds&8:&e" +
                addWorldInfo();
    }

    private static String addWorldInfo() {
        String worldInfoLoaded = "&6%s &7Chunks &8(&9%s&7 C&8) &e%s &7Entities &7&6%s &7Players";
        StringBuilder builder = new StringBuilder();

        for (ServerWorld world : KiloServer.getServer().getWorlds()) {
            MonitorableWorld monitoredWorld = (MonitorableWorld) world;

            if (monitoredWorld.totalLoadedChunks() != 0) {
                builder.append("\n&7* ").append(RegistryUtils.dimensionToName(world.dimension.getType())).append("&8: ");
                builder.append(String.format(worldInfoLoaded, monitoredWorld.totalLoadedChunks(), monitoredWorld.cachedChunks(), monitoredWorld.loadedEntities(), monitoredWorld.players()));
            }
        }

        return builder.toString();
    }

}
