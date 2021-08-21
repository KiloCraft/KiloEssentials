package org.kilocraft.essentials.mixin;

import net.minecraft.util.registry.RegistryKey;
import org.kilocraft.essentials.util.registry.RegistryKeyID;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryKey.class)
public abstract class RegistryKeyMixin<T> implements RegistryKeyID {

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
