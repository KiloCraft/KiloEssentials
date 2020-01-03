package org.kilocraft.essentials.config.datafixer;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.config.datafixer.type.main.ConfigSchemaV0;

/**
 * i509VCB: WIP, This is very complex so don't try to touch this yet
 */
public class ConfigDataFixer {
    private static final DataFixer dataFixer = create();

    private static DataFixer create() {
        DataFixerBuilder builder = new DataFixerBuilder(ModConstants.dataFixerSchema());
        Schema version0Schema = builder.addSchema(0, ConfigSchemaV0::new);

        return null;
    }

    public static DataFixer getInstance() {
        return dataFixer;
    }
}
