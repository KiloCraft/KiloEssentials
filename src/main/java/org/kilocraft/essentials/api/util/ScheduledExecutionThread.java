package org.kilocraft.essentials.api.util;

import net.minecraft.util.math.Vec3d;
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

    public static void teleport(OnlineUser player, ScheduledExecution s) {
        tick(3, player, player.asPlayer().getPos(), s);
        //TODO: Check if both players are still online
    }

    private static void tick(int seconds, OnlineUser player, Vec3d pos, ScheduledExecution s) {
        if (player.asPlayer().getPos().distanceTo(pos) > 1) {
            player.sendLangMessage("teleport.abort");
        } else {
            player.sendLangMessage("teleport.wait", seconds);
            start(1000, seconds == 1 ? s : () -> tick(seconds - 1, player, pos, s));
        }
    }

}
