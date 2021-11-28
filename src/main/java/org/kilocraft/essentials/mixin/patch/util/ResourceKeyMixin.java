package org.kilocraft.essentials.mixin.patch.util;

import net.minecraft.resources.ResourceKey;
import org.kilocraft.essentials.util.registry.IResourceKey;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ResourceKey.class)
public abstract class ResourceKeyMixin<T> implements IResourceKey {

    private int id = 0;

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public int getID() {
        return this.id;
    }

}
