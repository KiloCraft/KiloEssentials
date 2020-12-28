package org.kilocraft.essentials.api.util;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ScheduledExecutionThread {

    public static ConcurrentLinkedDeque<ScheduledExecution> scheduledExecutions = new ConcurrentLinkedDeque<>();

    public static void start(long wait, ScheduledExecution s) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(wait);
                scheduledExecutions.add(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });
    }

    public static void teleport(OnlineUser player, @Nullable OnlineUser player2, ScheduledExecution s) {
        teleport(player, player2, s, 1);
    }

    public static void teleport(OnlineUser player, @Nullable OnlineUser player2, ScheduledExecution s, int blocks) {
        int cooldown = KiloConfig.main().server().cooldown;
        if (cooldown < 1) s.apply();
        else tick(cooldown, player, player2, player.asPlayer().getPos(), s, blocks);
    }

    private static void tick(int seconds, OnlineUser player, @Nullable OnlineUser player2, Vec3d pos, ScheduledExecution s, int blocks) {
        if (player2 != null) {
            if (player.isOnline() && !player2.isOnline()) {
                player.sendLangMessage("teleport.offline", player2.getDisplayName());
                return;
            } else if (!player.isOnline() && player2.isOnline()) {
                player2.sendLangMessage("teleport.offline", player.getDisplayName());
                return;
            }
        }
        if (!player.isOnline()) {
            if (player.asPlayer().getPos().distanceTo(pos) > blocks) {
                player.sendLangMessage("teleport.abort");
                if (player2 != null) player2.sendLangMessage("teleport.abort.other", player.getDisplayName());
            } else {
                player.sendLangMessage("teleport.wait", seconds);
                start(1000, seconds == 1 ? s : () -> tick(seconds - 1, player, player2, pos, s, blocks));
            }
        }
    }

}
