package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.world.MonitorableWorld;
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.TpsTracker;
import org.kilocraft.essentials.util.monitor.SystemMonitor;
import org.kilocraft.essentials.util.registry.RegistryUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import static org.kilocraft.essentials.util.TpsTracker.*;

public class StatusCommand extends EssentialCommand {
    public StatusCommand() {
        super("status", CommandPermission.STATUS);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private static OperatingSystemMXBean bean = SystemMonitor.getOsSystemMXBean();

    private int execute(CommandContext<ServerCommandSource> ctx) {
        final CommandSourceUser sender = this.getCommandSource(ctx);
        try {
            sender.sendMessage(getInfo());
        } catch (Exception e) {
            String msg = "An unexpected exception occurred when processing the cpu usage" +
                    "Please report this to a Administrator";
            sender.sendMessage(
                    new LiteralText(msg + "\n Exception message:")
                            .append(new LiteralText(e.getMessage()).formatted(Formatting.WHITE)).formatted(Formatting.RED));

            KiloEssentials.getLogger().error(msg, e);
        }

        return SUCCESS;
    }

    private static String getInfo() throws Exception {
        double ramUsage = SystemMonitor.getRamUsedPercentage();
        double cpuUsage = SystemMonitor.getCpuLoadPercentage();
        return "&eGeneral status:&r\n" +
                "&7Platform: &6" + bean.getArch() + " &d" + Util.getOperatingSystem().name().toLowerCase() +
                "\n&7Server uptime: &6" + TimeDifferenceUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime()) +
                "\n&7TPS: " +
                String.format(
                        "%s <dark_gray>(<gray>%s ms<dark_gray>) <dark_gray>(<gray>5m<dark_gray>/<gray>15m<dark_gray>/<gray>1h<dark_gray>/<gray>1d<dark_gray>) %s<dark_gray>, %s<dark_gray>, %s<dark_gray>, %s<reset>",
                        ComponentText.formatTps(tps.getAverage()),
                        TpsTracker.MillisecondPerTick.getShortAverage(),
                        ComponentText.formatTps(tps5.getAverage()),
                        ComponentText.formatTps(tps15.getAverage()),
                        ComponentText.formatTps(tps60.getAverage()),
                        ComponentText.formatTps(tps1440.getAverage())) +
                "\n&7CPU &8(&e" + SystemMonitor.systemMXBean.getAvailableProcessors() + "&8)&7:" +
                " &" + ComponentText.formatPercentage(cpuUsage) + cpuUsage + "% Usage" +
                " &3" + Thread.activeCount() + " Running Threads" +
                "\n&7Memory &8(&e" + SystemMonitor.getRamMaxMB() + " max&8)&7: &" +
                ComponentText.formatPercentage(ramUsage) + ramUsage + "% " +
                "&8(&b" + SystemMonitor.getRamUsedMB() + " MB" + "&8/&7" +
                SystemMonitor.getRamTotalMB() + " MB" + "&8)" +
                "\n&7Worlds&8:&e" +
                addWorldInfo();
    }

    private static String addWorldInfo() {
        String worldInfoLoaded = "&6%s &7Chunks &8(&9%s&7 C&8) &7&6%s &7Players";
        StringBuilder builder = new StringBuilder();

        for (ServerWorld world : KiloServer.getServer().getWorlds()) {
            MonitorableWorld monitoredWorld = (MonitorableWorld) world;

            if (monitoredWorld.totalLoadedChunks() != 0) {
                builder.append("\n&7* ").append(RegistryUtils.dimensionToName(world.getDimension())).append("&8: ");
                builder.append(String.format(worldInfoLoaded, monitoredWorld.totalLoadedChunks(), monitoredWorld.cachedChunks(), monitoredWorld.players()));
            }
        }

        return builder.toString();
    }

}
