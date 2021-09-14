package org.kilocraft.essentials.util.settings.values;

import net.minecraft.nbt.NbtCompound;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;

public class CategorySetting extends AbstractSetting {

    public CategorySetting(String id) {
        super(id);
    }

    @Override
    public void toTag(NbtCompound tag) {
        NbtCompound setting = new NbtCompound();
        for (AbstractSetting child : this.children) {
            child.toTag(setting);
        }
        tag.put(this.id, setting);
    }

    @Override
    public void fromTag(NbtCompound tag) {
        if (tag.contains(this.id)) {
            NbtCompound setting = tag.getCompound(this.id);
            for (AbstractSetting child : this.children) {
                child.fromTag(setting);
            }
        }
    }
}
