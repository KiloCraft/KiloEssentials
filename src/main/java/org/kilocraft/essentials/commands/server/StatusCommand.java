package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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
import org.kilocraft.essentials.util.DataTracker;
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.TpsTracker;
import org.kilocraft.essentials.util.monitor.SystemMonitor;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

import static org.kilocraft.essentials.util.TpsTracker.*;

public class StatusCommand extends EssentialCommand {
    private static OperatingSystemMXBean bean = SystemMonitor.getOsSystemMXBean();

    public StatusCommand() {
        super("status", CommandPermission.STATUS);
    }

    private static Component getInfo() throws Exception {
        double ramUsage = SystemMonitor.getRamUsedPercentage();
        double cpuUsage = SystemMonitor.getCpuLoadPercentage();
        return ComponentText.of("&eGeneral status:&r\n" +
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
                "\n&7CPU &8(&e" + SystemMonitor.systemMXBean.getAvailableProcessors() + "&8)&7: " +
                ComponentText.formatPercentage(cpuUsage) + "% Usage" +
                " &3" + Thread.activeCount() + " Running Threads" +
                "\n&7Memory &8(&e" + SystemMonitor.getRamMaxMB() + " max&8)&7: " +
                ComponentText.formatPercentage(ramUsage) + "% " +
                "&8(&b" + SystemMonitor.getRamUsedMB() + " MB" + "&8/&7" +
                SystemMonitor.getRamTotalMB() + " MB" + "&8)" +
                "\n&7Worlds&8:&e").append(addWorldInfo())
                .append(ComponentText.of("\n&7Ticking: "))
                .append(ComponentText.of("&6BlockEntity ")
                        .append(ComponentText.of("&8(&a" + DataTracker.tickedBlockEntities.formattedAverage() + "&8/&7" + DataTracker.cTickedBlockEntities.formattedAverage() + "&8)"))
                .append(ComponentText.of(" &bEntity "))
                        .append(ComponentText.of("&8(&a" + DataTracker.tickedEntities.formattedAverage() + "&8/&7" + DataTracker.cTickedEntities.formattedAverage() + "&8)"))
                .append(ComponentText.of(" &aChunk "))
                        .append(ComponentText.of("&8(&a" + DataTracker.tickedChunks.formattedAverage() + "&8/&7" + DataTracker.cTickedChunks.formattedAverage() + "&8)"))
                .append(ComponentText.of(" &dSpawning "))
                        .append(ComponentText.of("&8(&a" + DataTracker.spawnAttempts.formattedAverage() + "&8/&7" + DataTracker.cSpawnAttempts.formattedAverage() + "&8)")));
    }

    private static TextComponent addWorldInfo() {
        String worldInfoLoaded = "&6%s &7Chunks &8(&9%s&7 C&8) &7&6%s &7Players &a%s &7Entities";
        TextComponent.Builder builder = Component.text();

        for (ServerWorld world : KiloServer.getServer().getWorlds()) {
            HashMap<ServerPlayerEntity, Integer> nearbyEntities = new HashMap<>();
            for (ServerPlayerEntity player : world.getPlayers()) {
                nearbyEntities.put(player, 0);
            }
            int entities = 0;
            for (Entity entity : world.iterateEntities()) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    int i = nearbyEntities.get(player);
                    if (entity.getPos().distanceTo(player.getPos()) < ServerSettings.VIEWDISTANCE.getValue() * 16) i++;
                    nearbyEntities.put(player, i);
                }
                entities++;
            }

            if (world.getChunkManager().getTotalChunksLoadedCount() != 0) {
                TextComponent.Builder hover = Component.text().content("Entities / Player:\n").color(NamedTextColor.YELLOW);
                for (Map.Entry<ServerPlayerEntity, Integer> entry : nearbyEntities.entrySet()) {
                    hover.append(ComponentText.of(entry.getKey().getEntityName() + ": ").color(NamedTextColor.GRAY)).append(Component.text(entry.getValue() + "\n").color(NamedTextColor.LIGHT_PURPLE));
                }
                builder.append(ComponentText.of("\n&7* " + RegistryUtils.dimensionToName(world.getDimension()) + "&8: "));
                builder.append(ComponentText.of(String.format(worldInfoLoaded, world.getChunkManager().getTotalChunksLoadedCount(), world.getChunkManager().getLoadedChunkCount(), world.getPlayers().size(), entities)).hoverEvent(HoverEvent.showText(hover.build())));
            }
        }
        return builder.build();
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

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

}
