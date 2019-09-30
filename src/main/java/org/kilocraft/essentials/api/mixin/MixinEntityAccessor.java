package org.kilocraft.essentials.api.mixin;

import org.kilocraft.essentials.api.util.EntityServerRayTraceable;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(Entity.class)
public abstract class MixinEntityAccessor implements EntityServerRayTraceable {
    @Shadow
    public World world;

    @Shadow
    public abstract Vec3d getCameraPosVec(float float_1);

    @Shadow
    public abstract Vec3d getRotationVec(float float_1);

    public HitResult rayTraceInServer(double double_1, float float_1, boolean boolean_1) {
        Vec3d vec3d_1 = this.getCameraPosVec(float_1);
        Vec3d vec3d_2 = this.getRotationVec(float_1);
        Vec3d vec3d_3 = vec3d_1.add(vec3d_2.x * double_1, vec3d_2.y * double_1, vec3d_2.z * double_1);
        return this.world.rayTrace(new RayTraceContext(vec3d_1, vec3d_3, RayTraceContext.ShapeType.OUTLINE, boolean_1 ? RayTraceContext.FluidHandling.ANY : RayTraceContext.FluidHandling.NONE, ((Entity) (Object) this)));
    }


}
