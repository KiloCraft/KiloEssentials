package org.kilocraft.essentials;

import net.minecraft.SharedConstants;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.util.TpsTracker;
import org.kilocraft.essentials.util.text.Texter;

import java.io.File;

public class KiloDebugUtils {
    private static final Logger LOGGER = LogManager.getLogger("KiloEssentials|Debug");
    public static KiloDebugUtils INSTANCE;
    private static boolean wasEnabled = false;
    private final MinecraftServer minecraftServer;
    private CommandBossBar bossBar;
    private final Identifier DEBUG_BAR = new Identifier("kiloessentials", "debug_bar");
    private static boolean barVisible = true;
    private static final String DEBUG_FORMAT = ModConstants.getProperties().getProperty("debug_bar_text");
    private static final MutableText DEBUG_TEXT = new LiteralText("[").formatted(Formatting.WHITE)
            .append(new LiteralText("Debug").styled(style -> style.withColor(TextColor.parse("#fefe16"))))
            .append("] ").formatted(Formatting.WHITE).formatted(Formatting.BOLD);

    public KiloDebugUtils() {
        INSTANCE = this;
        this.minecraftServer = KiloServer.getServer().getMinecraftServer();

        setupBossBar();
    }

    public static void validateDebugMode(boolean reload) {
        File debugFile = new File(KiloEssentials.getWorkingDirectory() + "/kiloessentials.debug");

        if (debugFile.exists()) {
            KiloEssentials.getServer().getLogger().warn("**** SERVER IS RUNNING IN DEBUG/DEVELOPMENT MODE!");
            KiloEssentials.getServer().getLogger().warn("     To change this simply remove the \"kiloessentials.debug\" file and reload");

            setDebugMode(true);
        } else {
            setDebugMode(false);
            if (INSTANCE != null) {
                INSTANCE.removeBossBar();
            }

            if (reload && wasEnabled) {
                KiloEssentials.getServer().getLogger().info("**** DEBUG/DEVELOPMENT MODE DISABLED!");
            }
        }
    }

    public static void setDebugMode(boolean set) {
        SharedConstants.isDevelopment = set;

        if (set && !wasEnabled && INSTANCE != null) {
            INSTANCE.setupBossBar();
            wasEnabled = true;
        }
    }

    public static void setDebugBarVisible(boolean set) {
        barVisible = set;
        if (INSTANCE != null) {
            INSTANCE.bossBar.setVisible(set);
        }
    }

    public static boolean shouldTick() {
        return INSTANCE != null && barVisible;
    }

    public void onTick() {
        if (SharedConstants.isDevelopment) {
            update();
        } else {
            removeBossBar();
        }
    }

    private void removeBossBar() {
        bossBar.clearPlayers();
        minecraftServer.getBossBarManager().remove(bossBar);
    }

    private void setupBossBar() {
        BossBarManager manager = minecraftServer.getBossBarManager();
        bossBar = manager.add(DEBUG_BAR, new LiteralText("DebugBar"));
        bossBar.setMaxValue(20);
        bossBar.setOverlay(BossBar.Style.PROGRESS);
    }

    public void update() {
        int loadedChunks = 0;
        for (ServerWorld world : minecraftServer.getWorlds()) {
            loadedChunks = loadedChunks + world.getChunkManager().getTotalChunksLoadedCount();
        }

        TpsTracker.tps.getAverage();

        String debugText = String.format(DEBUG_FORMAT,
                ComponentText.formatTps(TpsTracker.tps.getAverage()),
                TpsTracker.MillisecondPerTick.getShortAverage(), loadedChunks,
                ModConstants.getVersionInt(), ModConstants.getVersionNick()
        );

        bossBar.setName(Texter.newText().append(DEBUG_TEXT).append(Texter.newText(debugText)));

        int tps = (int) TpsTracker.tps.getAverage();
        bossBar.setValue(tps);

        if (tps > 15) {
            bossBar.setColor(BossBar.Color.GREEN);
        } else if (tps > 10) {
            bossBar.setColor(BossBar.Color.YELLOW);
        } else if (tps < 10) {
            bossBar.setColor(BossBar.Color.RED);
        } else {
            bossBar.setColor(BossBar.Color.PURPLE);
        }

        if (minecraftServer.getPlayerManager().getPlayerList() != null) {
            bossBar.addPlayers(minecraftServer.getPlayerManager().getPlayerList());
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

}
