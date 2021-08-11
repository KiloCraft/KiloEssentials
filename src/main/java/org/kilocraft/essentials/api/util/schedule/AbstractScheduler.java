package org.kilocraft.essentials.api.util.schedule;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class AbstractScheduler {

    public static ConcurrentLinkedDeque<ScheduledExecution> scheduledExecutions = new ConcurrentLinkedDeque<>();
    protected int countdown;

    public AbstractScheduler(int countdown) {
        this.countdown = countdown;
    }

    private static void start(long wait, ScheduledExecution s) {
        CompletableFuture.runAsync(() -> {
            Thread.currentThread().setName("KiloScheduler");
            try {
                Thread.sleep(wait);
                scheduledExecutions.add(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });
    }

    public abstract boolean onTick();

    public void tick() {
        if (countdown == 0) {
            onFinish();
        } else if (countdown > 0) {
            boolean success = onTick();
            countdown--;
            if (success) start(1000, this::tick);
        }
    }

    public abstract void onFinish();

}
