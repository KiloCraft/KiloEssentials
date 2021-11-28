package org.kilocraft.essentials.mixin.patch.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.kilocraft.essentials.api.util.EntityServerRayTraceable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityServerRayTraceMixin implements EntityServerRayTraceable {

    @Shadow
    public abstract Vec3 getEyePosition(float f);

    @Shadow
    public abstract Vec3 getViewVector(float f);

    @Shadow
    public Level level;

    /**
     * This is marked @Environment(EnvType.CLIENT) (not available in serverside code) so we need to re-implement it.
     */
    @Override
    public HitResult rayTrace(double maxDistance, float f, boolean passThroughFluids) {
        Vec3 cameraPos = this.getEyePosition(f);
        Vec3 rotationVec = this.getViewVector(f);
        Vec3 facingVec = cameraPos.add(rotationVec.x * maxDistance, rotationVec.y * maxDistance, rotationVec.z * maxDistance);
        return this.level.clip(
                new ClipContext(
                        cameraPos,
                        facingVec,
                        ClipContext.Block.OUTLINE,
                        passThroughFluids ? ClipContext.Fluid.NONE : ClipContext.Fluid.ANY,
                        ((Entity) (Object) this)
                )
        );
    }

}
