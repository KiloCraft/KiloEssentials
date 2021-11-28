package org.kilocraft.essentials.api.util.schedule;

import net.minecraft.world.phys.Vec3;
import org.kilocraft.essentials.api.user.OnlineUser;

public abstract class StandStillScheduler extends AbstractScheduler {

    protected final OnlineUser player;
    private final int blocks;
    private final Vec3 pos;

    public StandStillScheduler(OnlineUser player, int blocks, int countdown) {
        super(countdown);
        this.player = player;
        this.blocks = blocks;
        this.pos = player.asPlayer().position();
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
        return this.blocks >= 0 && this.player.asPlayer() != null && this.player.asPlayer().position().distanceTo(this.pos) > this.blocks;
    }

    abstract void sendAbortMessage();
}
