package org.kilocraft.essentials.api.util.schedule;

import org.kilocraft.essentials.api.user.OnlineUser;

public class TwoPlayerScheduler extends StandStillScheduler {

    private final OnlineUser receiver;

    public TwoPlayerScheduler(OnlineUser sender, OnlineUser receiver, int blocks, int countdown) {
        super(sender, blocks, countdown);
        this.receiver = receiver;
        this.tick();
    }

    @Override
    public boolean onTick() {
        if (this.player.isOnline() && !this.receiver.isOnline()) {
            this.player.sendLangMessage("teleport.offline", this.receiver.getDisplayName());
            return false;
        } else if (!this.player.isOnline() && this.receiver.isOnline()) {
            this.receiver.sendLangMessage("teleport.offline", this.player.getDisplayName());
            return false;
        }
        return super.onTick();
    }

    @Override
    void sendAbortMessage() {
        this.player.sendLangMessage("teleport.abort");
        if (this.receiver != null) this.receiver.sendLangMessage("teleport.abort.other", this.player.getDisplayName());
    }

    @Override
    public void onFinish() {
        if (this.moved()) {
            this.sendAbortMessage();
        } else {
            if (this.player.isOnline() && this.receiver.isOnline()) {
                this.player.teleport(this.receiver);
            }
        }
    }
}
