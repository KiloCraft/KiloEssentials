package org.kilocraft.essentials.mixin;

import net.minecraft.scoreboard.Scoreboard;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.config.KiloConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.Map;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin {
    @Redirect(method = "addPlayerToTeam", at = @At(value = "INVOKE", target = "Ljava/util/Collection;add(Ljava/lang/Object;)Z"))
    private <E> boolean modify$OnAdd(Collection<E> collection, E e) {
        if (KiloConfig.main().playerList().useNicknames && e instanceof String) {
            KiloServer.getServer().getMetaManager().updateDisplayName((String) e);
        }

        return collection.add(e);
    }

    @Redirect(method = "removePlayerFromTeam", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"))
    private <V> Object modify$OnRemove(Map map, Object key) {
        if (KiloConfig.main().playerList().useNicknames && key instanceof String) {
            KiloServer.getServer().getMetaManager().updateDisplayName((String) key);
        }

        return map.remove(key);
    }
}
