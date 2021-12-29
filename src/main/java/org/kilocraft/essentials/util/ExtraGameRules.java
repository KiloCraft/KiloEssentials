package org.kilocraft.essentials.util;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;
import org.kilocraft.essentials.api.KiloEssentials;

public class ExtraGameRules {

    public static final GameRules.Key<GameRules.BooleanValue> BROADCAST_ADMIN_COMMANDS = register("broadcastAdminCommands", GameRules.Category.CHAT, GameRuleFactory.createBooleanRule(false));

    private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type) {
        return GameRuleRegistry.register(name, category, type);
    }

    public static void initialize() {
        KiloEssentials.getLogger().info("Registering custom gamerules...");
    }

}
