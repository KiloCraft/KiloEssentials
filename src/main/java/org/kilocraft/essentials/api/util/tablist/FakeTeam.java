package org.kilocraft.essentials.api.util.tablist;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;

/**
 * This is a dummy class for teams used in {@link TabListData}
 * to allow for fake team packets, which don't interfere with
 * any scoreboard data. {@link org.kilocraft.essentials.mixin.patch.util.TeamMixin}
 * is used to cancel any calls to {@link Scoreboard#updateScoreboardTeam(Team)},
 * which would otherwise throw a {@link NullPointerException}.
 */
public class FakeTeam extends Team {

    public FakeTeam(String name) {
        super(null, name);
    }

    @Override
    public Scoreboard getScoreboard() {
        throw new UnsupportedOperationException();
    }

}
