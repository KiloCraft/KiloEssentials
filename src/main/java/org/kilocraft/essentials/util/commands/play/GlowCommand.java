package org.kilocraft.essentials.util.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.Objects;

public class GlowCommand extends EssentialCommand {
    public GlowCommand () {
        super("glow", CommandPermission.GLOW);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::set);
    }

    private int set(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);
        if (user.asPlayer().getStatusEffect(StatusEffects.GLOWING) != null && Objects.requireNonNull(user.asPlayer().getStatusEffect(StatusEffects.GLOWING)).isAmbient()) {
            user.asPlayer().removeStatusEffect(StatusEffects.GLOWING);
            user.sendLangMessage("command.glow.disable");

            return FAILED;
        }

        user.asPlayer().addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 255, true, false, false));
        user.sendLangMessage("command.glow.enable");
        return SUCCESS;
    }
}
