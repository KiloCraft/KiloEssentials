package org.kilocraft.essentials.util.settings.values.util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

public class RootSetting implements Setting {

    private final List<AbstractSetting> children = new ArrayList<>();
    private final HashMap<String, AbstractSetting> cache = new HashMap<>();

    public RootSetting() {

    }

    public RootSetting addChild(AbstractSetting setting) {
        this.children.add(setting);
        setting.setParent(this);
        return this;
    }

    @Override
    public List<AbstractSetting> getChildren() {
        return this.children;
    }

    @Override
    public String getFullId() {
        return "";
    }

    @Nullable
    public Setting getSetting(String id) {
        if (id.equals("")) return this;
        AbstractSetting cached = this.cache.get(id);
        if (cached != null) {
            return cached;
        } else {
            for (AbstractSetting child : this.children) {
                if (id.startsWith(child.id)) {
                    String[] part = id.split("\\.", 2);
                    AbstractSetting result = child.getSetting(part.length > 1 ? part[1] : "");
                    this.cache.put(id, result);
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
        for (AbstractSetting child : this.children) {
            child.fromTag(tag);
        }
    }
}
