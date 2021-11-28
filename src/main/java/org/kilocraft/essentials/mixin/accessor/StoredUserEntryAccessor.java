package org.kilocraft.essentials.mixin.accessor;

import net.minecraft.server.players.StoredUserEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StoredUserEntry.class)
public interface StoredUserEntryAccessor<T> {

    @Accessor("user")
    T getUser();

}
