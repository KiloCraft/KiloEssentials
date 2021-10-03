package org.kilocraft.essentials.api.util.schedule;

import org.kilocraft.essentials.api.user.OnlineUser;

public class SinglePlayerScheduler extends StandStillScheduler {

    private final ScheduledExecution scheduled;

    public SinglePlayerScheduler(OnlineUser sender, int blocks, int countdown, ScheduledExecution scheduled) {
        super(sender, blocks, countdown);
        this.scheduled = scheduled;
        this.tick();
    }

    @Override
    public boolean onTick() {
        return super.onTick();
    }

    @Override
    void sendAbortMessage() {
        this.player.sendLangMessage("teleport.abort");
    }

    @Override
    public void onFinish() {
        if (this.moved()) {
            this.sendAbortMessage();
        } else {
            if (this.player.isOnline()) this.scheduled.apply();
        }
    }
}
