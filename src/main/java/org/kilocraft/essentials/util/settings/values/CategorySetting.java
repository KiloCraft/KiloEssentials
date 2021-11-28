package org.kilocraft.essentials.util.settings.values;

import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.util.settings.values.util.AbstractSetting;

public class CategorySetting extends AbstractSetting {

    public CategorySetting(String id) {
        super(id);
    }

    @Override
    public void toTag(CompoundTag tag) {
        CompoundTag setting = new CompoundTag();
        for (AbstractSetting child : this.children) {
            child.toTag(setting);
        }
        tag.put(this.id, setting);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (tag.contains(this.id)) {
            CompoundTag setting = tag.getCompound(this.id);
            for (AbstractSetting child : this.children) {
                child.fromTag(setting);
            }
        }
    }
}
