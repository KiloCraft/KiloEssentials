package org.kilocraft.essentials.config.datafixer;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import org.kilocraft.essentials.api.ModConstants;

/**
 * i509VCB: WIP, This is very complex so don't try to touch this yet
 */
public class ConfigDataFixer {
    private static final DataFixer dataFixer = create();

    private static DataFixer create() {
        DataFixerBuilder builder = new DataFixerBuilder(ModConstants.dataFixerSchema());
        return null;
    }

    public static DataFixer getInstance() {
        return dataFixer;
    }
}
