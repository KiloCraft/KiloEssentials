package org.kilocraft.essentials.util.settings.values.util;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RootSetting implements Setting {

    private final List<AbstractSetting> children = new ArrayList<>();
    private final HashMap<String, AbstractSetting> cache = new HashMap<>();


    public RootSetting() {

    }

    public RootSetting addChild(AbstractSetting setting) {
        children.add(setting);
        setting.setParent(this);
        return this;
    }

    public List<AbstractSetting> getChildren() {
        List<AbstractSetting> result = new ArrayList<>();
        for (AbstractSetting child : children) {
            result.addAll(child.getChildren());
        }
        return result;
    }

    @Override
    public String getID() {
        return "";
    }

    @Nullable
    public AbstractSetting getSetting(String id) {
        AbstractSetting cached = cache.get(id);
        if (cached != null) {
            return cached;
        } else {
            for (AbstractSetting child : children) {
                if (id.startsWith(child.id)) {
                    String[] part = id.split("\\.", 2);
                    AbstractSetting result = child.getSetting(part.length > 1 ? part[1] : "");
                    cache.put(id, result);
                    return result;
                }
            }
            return null;
        }
    }

    public CompoundTag toTag() {
        CompoundTag root = new CompoundTag();
        for (AbstractSetting child : this.children) {
            child.toTag(root);
        }
        return root;
    }

    public void fromTag(CompoundTag tag) {
        for (AbstractSetting child : children) {
            child.fromTag(tag);
        }
    }
}
