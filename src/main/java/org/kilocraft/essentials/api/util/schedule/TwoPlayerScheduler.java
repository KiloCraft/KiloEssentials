package org.kilocraft.essentials.api.util.schedule;

import org.kilocraft.essentials.api.user.OnlineUser;

public class TwoPlayerScheduler extends StandStillScheduler {

    private final OnlineUser receiver;

    public TwoPlayerScheduler(OnlineUser sender, OnlineUser receiver, int blocks, int countdown) {
        super(sender, blocks, countdown);
        this.receiver = receiver;
        tick();
    }

    @Override
    public boolean onTick() {
        if (player.isOnline() && !receiver.isOnline()) {
            player.sendLangMessage("teleport.offline", receiver.getDisplayName());
            return false;
        } else if (!player.isOnline() && receiver.isOnline()) {
            receiver.sendLangMessage("teleport.offline", player.getDisplayName());
            return false;
        }
        return super.onTick();
    }

    @Override
    void sendAbortMessage() {
        player.sendLangMessage("teleport.abort");
        if (receiver != null) receiver.sendLangMessage("teleport.abort.other", player.getDisplayName());
    }

    @Override
    public void onFinish() {
        if (moved()) {
            sendAbortMessage();
        } else {
            if (player.isOnline() && receiver.isOnline()) {
                player.teleport(receiver);
            }
        }
    }
}
