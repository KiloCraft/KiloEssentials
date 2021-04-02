package org.kilocraft.essentials.util.settings.values.util;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSetting implements Setting {

    protected final String id;
    protected final List<AbstractSetting> children = new ArrayList<>();
    Setting parent;


    public AbstractSetting(String id) {
        this.id = id;
    }


    public abstract void toTag(NbtCompound tag);

    public abstract void fromTag(NbtCompound tag);

    public AbstractSetting addChild(AbstractSetting setting) {
        children.add(setting);
        setting.setParent(this);
        return this;
    }

    public List<AbstractSetting> getChildren() {
        List<AbstractSetting> result = new ArrayList<>();
        result.add(this);
        for (AbstractSetting child : children) {
            result.addAll(child.getChildren());
        }
        return result;
    }

    protected void setParent(Setting parent) {
        this.parent = parent;
    }

    public String getID() {
        String parentID = parent.getID();
        return parentID.equals("") ? id : parentID + "." + id;
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



}
