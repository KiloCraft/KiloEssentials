package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.util.EntityServerRayTraceable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityServerRayTraceMixin implements EntityServerRayTraceable {

    @Shadow public abstract Vec3d getCameraPosVec(float tickDelta);

    @Shadow public abstract Vec3d getRotationVec(float tickDelta);

    @Shadow public World world;

    // This is marked @Environment(EnvType.CLIENT) (not available in serverside code) so we need to re-implement it.
    @Override
    public HitResult rayTrace(double maxDistance, float f, boolean passThroughFluids) {
        Vec3d cameraPos = this.getCameraPosVec(f);
        Vec3d rotationVec = this.getRotationVec(f);
        Vec3d facingVec = cameraPos.add(rotationVec.x * maxDistance, rotationVec.y * maxDistance, rotationVec.z * maxDistance);
        return this.world.raycast(
                new RaycastContext(
                        cameraPos,
                        facingVec,
                        RaycastContext.ShapeType.OUTLINE,
                        passThroughFluids ? RaycastContext.FluidHandling.NONE : RaycastContext.FluidHandling.ANY,
                        ((Entity) (Object) this)
                )
        );    }

}
