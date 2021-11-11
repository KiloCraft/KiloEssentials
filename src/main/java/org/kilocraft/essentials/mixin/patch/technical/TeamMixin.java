package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.kilocraft.essentials.api.util.tablist.FakeTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Team.class)
public abstract class TeamMixin {

    @Shadow
    @Final
    private Scoreboard scoreboard;

    @Redirect(
            method = {"setDisplayName", "setPrefix", "setSuffix", "setFriendlyFireAllowed", "setShowFriendlyInvisibles", "setNameTagVisibilityRule", "setDeathMessageVisibilityRule", "setCollisionRule", "setColor"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"
            )
    )
    public void dontUpdateScoreboardOnFakeTeam(Scoreboard scoreboard, Team team) {
        Team thisTeam = (Team) (Object) this;
        if (!(thisTeam instanceof FakeTeam)) this.scoreboard.updateScoreboardTeam(thisTeam);
    }


}
