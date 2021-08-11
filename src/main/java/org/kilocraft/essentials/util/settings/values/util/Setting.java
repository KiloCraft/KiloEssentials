package org.kilocraft.essentials.util.settings.values.util;

import java.util.List;

public interface Setting {

    String getFullId();

    List<AbstractSetting> getChildren();

}
