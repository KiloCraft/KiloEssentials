package org.kilocraft.essentials.util.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.util.TickManager;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.monitor.SystemMonitor;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class StatusCommand extends EssentialCommand {

    public StatusCommand() {
        super("status", CommandPermission.STATUS);
    }

    private static Component getInfo() throws Exception {
        double ramUsage = SystemMonitor.getRamUsedPercentage();
        double cpuUsage = SystemMonitor.getCpuLoadPercentage();
        return ComponentText.of("&eGeneral status:&r\n" +
                "\n&7Server uptime: &6" + TimeDifferenceUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime()) +
                "\n&7TPS: " +
                String.format(
                        "<gold>TPS %s <dark_gray>(<gray>%s ms<dark_gray>) <dark_gray>(<gray>1m<dark_gray>/<gray>5m<dark_gray>/<gray>15m<dark_gray>/<gray>1h<dark_gray>) %s<dark_gray>, %s<dark_gray>, %s<dark_gray>, %s<reset>",
                        ComponentText.formatTps(TickManager.tps[0]),
                        TickManager.getFormattedMSPT(),
                        ComponentText.formatTps(TickManager.tps[2]),
                        ComponentText.formatTps(TickManager.tps[3]),
                        ComponentText.formatTps(TickManager.tps[4]),
                        ComponentText.formatTps(TickManager.tps[5])) +
                "\n&7CPU: " +
                ComponentText.formatPercentage(cpuUsage) + "% Usage" +
                " &3" + Thread.activeCount() + " Running Threads" +
                "\n&7Memory &8(&e" + SystemMonitor.getRamMaxMB() + " max&8)&7: " +
                ComponentText.formatPercentage(ramUsage) + "% " +
                "&8(&b" + SystemMonitor.getRamUsedMB() + " MB" + "&8/&7" +
                SystemMonitor.getRamTotalMB() + " MB" + "&8)" +
                "\n&7Worlds&8:&e").append(addWorldInfo());
    }

    private static TextComponent addWorldInfo() {
        String worldInfoLoaded = "&6%s &7Chunks &8(&9%s&7 C&8) &7&6%s &7Players &a%s &7Entities";
        TextComponent.Builder builder = Component.text();

        for (ServerLevel world : KiloEssentials.getMinecraftServer().getAllLevels()) {
            HashMap<ServerPlayer, Integer> nearbyEntities = new HashMap<>();
            for (ServerPlayer player : world.players()) {
                nearbyEntities.put(player, 0);
            }
            int entities = 0;
            for (Entity entity : world.getAllEntities()) {
                for (ServerPlayer player : world.players()) {
                    int i = nearbyEntities.get(player);
                    if (entity.position().distanceTo(player.position()) < ServerSettings.getViewDistance() * 16) i++;
                    nearbyEntities.put(player, i);
                }
                entities++;
            }

            if (world.getChunkSource().getTickingGenerated() != 0) {
                TextComponent.Builder hover = Component.text().content("Entities / Player:\n").color(NamedTextColor.YELLOW);
                for (Map.Entry<ServerPlayer, Integer> entry : nearbyEntities.entrySet()) {
                    hover.append(ComponentText.of(entry.getKey().getScoreboardName() + ": ").color(NamedTextColor.GRAY)).append(Component.text(entry.getValue() + "\n").color(NamedTextColor.LIGHT_PURPLE));
                }
                builder.append(ComponentText.of("\n&7* " + RegistryUtils.dimensionToName(world.dimensionType()) + "&8: "));
                builder.append(ComponentText.of(String.format(worldInfoLoaded, world.getChunkSource().getTickingGenerated(), world.getChunkSource().getLoadedChunksCount(), world.players().size(), entities)).hoverEvent(HoverEvent.showText(hover.build())));
            }
        }
        return builder.build();
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        final CommandSourceUser sender = this.getCommandSource(ctx);
        try {
            sender.sendMessage(getInfo());
        } catch (Exception e) {
            String msg = "An unexpected exception occurred when processing the cpu usage" +
                    "Please report this to a Administrator";
            sender.sendMessage(
                    new net.minecraft.network.chat.TextComponent(msg + "\n Exception message:")
                            .append(new net.minecraft.network.chat.TextComponent(e.getMessage()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.RED));

            KiloEssentials.getLogger().error(msg, e);
        }

        return SUCCESS;
    }

}
