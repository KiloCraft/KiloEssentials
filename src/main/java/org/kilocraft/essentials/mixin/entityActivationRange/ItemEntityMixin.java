package org.kilocraft.essentials.mixin.entityActivationRange;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.patch.entityActivationRange.InactiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements InactiveEntity {


    @Shadow
    private int age;

    @Shadow
    private int pickupDelay;

    private int lastTick = KiloEssentials.getServer().getMinecraftServer().getTicks();

    public ItemEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void inactiveTick() {
        int elapsedTicks = KiloEssentials.getServer().getMinecraftServer().getTicks() - this.lastTick;
        if (this.pickupDelay > 0 && this.pickupDelay != 32767)
            this.pickupDelay = Math.max((this.pickupDelay - elapsedTicks), 0);
        if (this.age != -32768) this.age = elapsedTicks;
        this.lastTick = KiloEssentials.getServer().getMinecraftServer().getTicks();

        if (!this.world.isClient && this.age >= 6000) {
            this.discard();
        }
    }
}
