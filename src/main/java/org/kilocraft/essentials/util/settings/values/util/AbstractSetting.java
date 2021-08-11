package org.kilocraft.essentials.util.settings.values.util;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSetting implements Setting {

    protected final String id;
    protected final List<AbstractSetting> children = new ArrayList<>();
    Setting parent;
    private boolean limitChildren = false;

    public AbstractSetting(String id) {
        this.id = id;
    }


    public abstract void toTag(NbtCompound tag);

    public abstract void fromTag(NbtCompound tag);

    @Override
    public List<AbstractSetting> getChildren() {
        return children;
    }

    public boolean shouldLimitChildren() {
        return limitChildren;
    }


    public AbstractSetting addChild(AbstractSetting setting) {
        children.add(setting);
        setting.setParent(this);
        return this;
    }

    public List<AbstractSetting> getChildrenRecursive() {
        List<AbstractSetting> result = new ArrayList<>();
        result.add(this);
        for (AbstractSetting child : children) {
            result.addAll(child.getChildrenRecursive());
        }
        return result;
    }

    protected void setParent(Setting parent) {
        this.parent = parent;
    }

    public String getFullId() {
        String parentID = parent.getFullId();
        return parentID.equals("") ? id : parentID + "." + id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public AbstractSetting getSetting(String id) {
        if (id.equals("")) {
            return this;
        }
        for (AbstractSetting child : children) {
            if (id.startsWith(child.id)) {
                String[] part = id.split("\\.", 2);
                return child.getSetting(part.length > 1 ? part[1] : "");
            }
        }
        return null;
    }

    public AbstractSetting limitChildren() {
        this.limitChildren = true;
        return this;
    }

}
