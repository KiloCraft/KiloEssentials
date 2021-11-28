package org.kilocraft.essentials.api.util.tablist;

import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.kilocraft.essentials.mixin.patch.technical.PlayerTeamMixin;

/**
 * This is a dummy class for teams used in {@link LuckpermsTabListData}
 * to allow for fake team packets, which don't interfere with
 * any scoreboard data. {@link PlayerTeamMixin}
 * is used to cancel any calls to {@link Scoreboard#onTeamChanged(PlayerTeam)},
 * which would otherwise throw a {@link NullPointerException}.
 */
public class FakeTeam extends PlayerTeam {

    public FakeTeam(String name) {
        super(null, name);
    }

    @Override
    public Scoreboard getScoreboard() {
        throw new UnsupportedOperationException();
    }

}
