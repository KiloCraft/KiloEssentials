package org.kilocraft.essentials.api.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.util.SomeGlobals;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CommandManager.class})
public abstract class MixinCommandManager {

    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "<init>", at = {@At("RETURN")})
    public void CommandManager(boolean boolean_1, CallbackInfo ci) {
        SomeGlobals.commandDispatcher = dispatcher;
        LOGGER.info("KiloAPI: CommandDispatcher set to {}", dispatcher);
    }

}
