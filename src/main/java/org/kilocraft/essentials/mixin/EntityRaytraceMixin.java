package org.kilocraft.essentials.mixin;

import org.kilocraft.essentials.api.util.EntityServerRayTraceable;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
@Implements(@Interface(iface = EntityServerRayTraceable.class, prefix = "server$")) // We use implements cause it's a bit cleaner and will allow method to simply be called rayTrace inside of EntityServerRayTraceable.
public abstract class EntityRaytraceMixin {
    @Shadow
    public World world;

    @Shadow
    public abstract Vec3d getCameraPosVec(float float_1);

    @Shadow
    public abstract Vec3d getRotationVec(float float_1);

    public HitResult server$rayTrace(double double_1, float float_1, boolean passesThroughFluid) {
        Vec3d cameraPos = this.getCameraPosVec(float_1);
        Vec3d rotationVec = this.getRotationVec(float_1);
        Vec3d facingVec = cameraPos.add(rotationVec.x * double_1, rotationVec.y * double_1, rotationVec.z * double_1);
        return this.world.rayTrace(new RayTraceContext(cameraPos, facingVec, RayTraceContext.ShapeType.OUTLINE, passesThroughFluid ? RayTraceContext.FluidHandling.ANY : RayTraceContext.FluidHandling.NONE, ((Entity) (Object) this)));
    }
}
