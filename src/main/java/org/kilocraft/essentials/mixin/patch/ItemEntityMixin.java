package org.kilocraft.essentials.mixin.patch;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "tryMerge()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;getBoundingBox()Lnet/minecraft/util/math/Box;"))
    public Box adjustMergeRadius(ItemEntity itemEntity) {
        return this.getBoundingBox().expand(ServerSettings.ITEM_MERGE_RADIUS.getValue(), 0.0D, ServerSettings.ITEM_MERGE_RADIUS.getValue());
    }

}
