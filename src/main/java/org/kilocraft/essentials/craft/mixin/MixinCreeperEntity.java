package org.kilocraft.essentials.craft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.explosion.Explosion;

@Mixin(CreeperEntity.class)
public class MixinCreeperEntity {

	@Shadow int explosionRadius;
	@Shadow private void spawnEffectsCloud() {}

	private void explode() {
		CreeperEntity creeper = (CreeperEntity) (Object) this;
		if (!creeper.world.isClient) {
			float float_1 = creeper.shouldRenderOverlay() ? 2.0F : 1.0F;
			System.out.println("TEST");
			creeper.world.createExplosion(creeper, creeper.getX(), creeper.getY(), creeper.getZ(),
					(float) explosionRadius * float_1, Explosion.DestructionType.NONE);
			creeper.remove();
			spawnEffectsCloud();
		}
	}
}
