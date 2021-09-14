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
        if (this.player.isOnline()) {
            if (this.moved()) {
                this.sendAbortMessage();
                return false;
            } else {
                this.player.sendLangMessage("teleport.wait", this.countdown);
                return true;
            }
        }
        return false;
    }

    protected boolean moved() {
        return this.blocks >= 0 && this.player.asPlayer() != null && this.player.asPlayer().getPos().distanceTo(this.pos) > this.blocks;
    }

    abstract void sendAbortMessage();
}
