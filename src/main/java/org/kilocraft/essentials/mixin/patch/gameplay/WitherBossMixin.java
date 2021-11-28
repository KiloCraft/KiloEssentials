package org.kilocraft.essentials.mixin.patch.gameplay;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin extends Monster {

    @Shadow
    public abstract int getInvulnerableTicks();

    protected WitherBossMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    // Move withers out of bedrock traps
    @Inject(
            method = "aiStep",
            at = @At("HEAD")
    )
    public void moveIfInBedrock(CallbackInfo ci) {
        if (this.getInvulnerableTicks() > 0) {
            if (this.getCommandSenderWorld().getBlockState(this.blockPosition().offset(0, ServerSettings.wither_check_distance, 0)).is(BlockTags.WITHER_IMMUNE)
                    || this.getCommandSenderWorld().getBlockState(this.blockPosition().offset(0, ServerSettings.wither_check_distance + 1, 0)).is(BlockTags.WITHER_IMMUNE)) {
                this.absMoveTo(this.position().x, (this.position().y + ServerSettings.wither_tp_distance), this.position().z);
            }
        }
    }

}
