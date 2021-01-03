package org.kilocraft.essentials.util.settings;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpawnGroupValue extends Value<Float> {

    protected final static HashMap<Identifier, List<SpawnGroupValue>> byWorld = new HashMap<>();
    protected final Identifier world;
    @Nullable
    protected final SpawnGroup spawnGroup;

    SpawnGroupValue(Identifier world, @Nullable SpawnGroup spawnGroup, Float value) {
        super(value);
        this.world = world;
        this.spawnGroup = spawnGroup;
        List<SpawnGroupValue> list = byWorld.getOrDefault(world, new ArrayList<>());
        list.add(this);
        byWorld.put(world, list);
    }

    static SpawnGroupValue get(Identifier world, @Nullable SpawnGroup spawnGroup) {
        if (byWorld.containsKey(world)) {
            for (SpawnGroupValue value : byWorld.get(world)) {
                if (value.getID().equals(getID(spawnGroup)) && world.equals(value.world)) return value;
            }
        }
        return new SpawnGroupValue(world, spawnGroup, 1F);
    }

    static String getID(SpawnGroup spawnGroup) {
        return spawnGroup == null ? "global" : spawnGroup.getName().toLowerCase();
    }

    String getID() {
        return getID(spawnGroup);
    }

}
