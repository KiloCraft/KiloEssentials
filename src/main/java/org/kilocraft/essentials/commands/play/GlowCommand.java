package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;

public class GlowCommand extends EssentialCommand {

    public GlowCommand () {
        super("glow", CommandPermission.GLOW);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(ctx -> {
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            if (player.hasStatusEffect(StatusEffects.GLOWING) && player.getStatusEffect(StatusEffects.GLOWING).isAmbient()) {
                // Disable
                player.removeStatusEffect(StatusEffects.GLOWING);
                KiloChat.sendLangMessageTo(player, "command.glow.disable");
            } else {
                // Enable
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 255, true, false, false));
                KiloChat.sendLangMessageTo(player, "command.glow.enable");
            }

            return 0;
        });
    }
}
