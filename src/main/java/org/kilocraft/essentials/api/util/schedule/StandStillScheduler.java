package org.kilocraft.essentials.api.util.schedule;

import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.user.OnlineUser;

public abstract class StandStillScheduler extends AbstractScheduler {

    protected final OnlineUser player;
    private final int blocks;
    private final Vec3d pos;

    public StandStillScheduler(OnlineUser player, int blocks, int countdown) {
        super(countdown);
        this.player = player;
        this.blocks = blocks;
        this.pos = player.asPlayer().getPos();
    }

    @Override
    public boolean onTick() {
        if (player.isOnline()) {
            if (moved()) {
                sendAbortMessage();
                return false;
            } else {
                player.sendLangMessage("teleport.wait", this.countdown);
                return true;
            }
        }
        return false;
    }

    protected boolean moved() {
        return blocks >= 0 && player.asPlayer().getPos().distanceTo(pos) > blocks;
    }

    abstract void sendAbortMessage();
}
