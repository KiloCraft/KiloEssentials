package org.kilocraft.essentials.api.util;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.concurrent.CompletableFuture;

public class ScheduledExecutionThread {

    public static void start(long wait, ScheduledExecution s) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(wait);
                s.apply();
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
        s.apply();
//        tick(3, player, player2, player.asPlayer().getPos(), s, blocks);
        //TODO: Check if both players are still online
    }

    private static void tick(int seconds, OnlineUser player, @Nullable OnlineUser player2, Vec3d pos, ScheduledExecution s, int blocks) {
        if (player2 != null) {
            if (isOnline(player) && !isOnline(player2)) {
                player.sendLangMessage("teleport.offline", player2.getDisplayName());
                return;
            } else if (!isOnline(player) && isOnline(player2)) {
                player2.sendLangMessage("teleport.offline", player.getDisplayName());
                return;
            }
        }
        if (player.asPlayer().getPos().distanceTo(pos) > blocks) {
            player.sendLangMessage("teleport.abort");
            if (player2 != null) player2.sendLangMessage("teleport.abort.other", player.getDisplayName());
        } else {
            player.sendLangMessage("teleport.wait", seconds);
            start(1000, seconds == 1 ? s : () -> tick(seconds - 1, player, player2, pos, s, blocks));
        }
    }

    private static boolean isOnline(OnlineUser user) {
        return KiloServer.getServer().getPlayerManager().getPlayer(user.getUuid()) != null;
    }

}
