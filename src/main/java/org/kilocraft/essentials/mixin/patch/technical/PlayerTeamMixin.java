package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.kilocraft.essentials.api.util.tablist.FakeTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerTeam.class)
public abstract class PlayerTeamMixin {

    @Shadow
    @Final
    private Scoreboard scoreboard;

    @Redirect(
            method = {"setDisplayName", "setPlayerPrefix", "setPlayerSuffix", "setAllowFriendlyFire", "setSeeFriendlyInvisibles", "setNameTagVisibility", "setDeathMessageVisibility", "setCollisionRule", "setColor"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"
            )
    )
    public void dontUpdateScoreboardOnFakeTeam(Scoreboard scoreboard, PlayerTeam team) {
        PlayerTeam thisTeam = (PlayerTeam) (Object) this;
        if (!(thisTeam instanceof FakeTeam)) this.scoreboard.onTeamChanged(thisTeam);
    }


}
