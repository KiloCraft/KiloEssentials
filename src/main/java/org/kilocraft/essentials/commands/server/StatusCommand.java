package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.world.MonitorableWorld;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.monitor.SystemMonitor;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Objects;

import static org.kilocraft.essentials.api.chat.TextFormat.getFormattedTPS;
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
            KiloChat.sendMessageToSource(ctx.getSource(), new ChatMessage(getInfo(), true));
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
        return "&eGeneral status:&r\n" +
                "&7- Platform: &6" + bean.getArch() + " &d" + System.getProperty("os.name") +
                "\n&7- Server uptime: &6" + TimeDifferenceUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime()) +
                "\n&7- TPS:" +
                String.format("&%s %s&8,&8(&75m&8/&715m&8/&730m&8&8/&71h&8)&%s %s&8,&%s %s&8,&%s %s&8,&%s %s&r",
                        getFormattedTPS(tps1.getAverage()), tps1.getShortAverage(), getFormattedTPS(tps5.getAverage()), tps5.getShortAverage(),
                        getFormattedTPS(tps15.getAverage()), tps15.getShortAverage(), getFormattedTPS(tps30.getAverage()), tps30.getShortAverage(),
                        getFormattedTPS(tps60.getAverage()), tps60.getShortAverage()) +
                "\n&7- CPU &8(&e" + SystemMonitor.systemMXBean.getAvailableProcessors() + "&8)&7:" +
                " &6" + SystemMonitor.getCpuLoadPercentage() + "% Usage" +
                " &e" + Thread.activeCount() + " Running Threads" +
                "\n&7- Memory &8(&e" + SystemMonitor.getRamMaxMB() + " max&8)&7: &6" +
                SystemMonitor.getRamUsedPercentage() + "% " +
                "&8(&e" + SystemMonitor.getRamUsedMB() + " MB" + "&8/&e" +
                SystemMonitor.getRamTotalMB() + " MB" + "&8)" +
                "\n&7- Worlds&8:&e" +
                addWorldInfo();
    }

    private static String addWorldInfo() {
        String worldInfoLoaded = "&6%s Chunks &e%s Entities &6%s Players";
        String worldInfoNotLoaded = "&7&oNot loaded.";
        StringBuilder builder = new StringBuilder();

        for (ServerWorld world : KiloServer.getServer().getWorlds()) {
            MonitorableWorld monitoredWorld = (MonitorableWorld) world;
            builder.append("\n&7 - ").append(getWorldName(world)).append("&8: ");

            if (monitoredWorld.totalLoadedChunks() != 0)
                builder.append(String.format(worldInfoLoaded, monitoredWorld.totalLoadedChunks(), monitoredWorld.loadedEntities(), monitoredWorld.players()));
            else
                builder.append(worldInfoNotLoaded);
        }

        return builder.toString();
    }

    private static String getWorldName(ServerWorld world) {
        String s = Objects.requireNonNull(Registry.DIMENSION_TYPE.getId(world.dimension.getType())).getPath();
        return s.replaceFirst(String.valueOf(s.charAt(0)), String.valueOf(s.charAt(0)).toUpperCase());
    }

}
