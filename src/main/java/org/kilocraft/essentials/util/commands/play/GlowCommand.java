package org.kilocraft.essentials.util.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.CommandPermission;

import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class GlowCommand extends EssentialCommand {
    public GlowCommand() {
        super("glow", CommandPermission.GLOW);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(this::set);
    }

    private int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);
        if (user.asPlayer().getEffect(MobEffects.GLOWING) != null && Objects.requireNonNull(user.asPlayer().getEffect(MobEffects.GLOWING)).isAmbient()) {
            user.asPlayer().removeEffect(MobEffects.GLOWING);
            user.sendLangMessage("command.glow.disable");

            return FAILED;
        }

        user.asPlayer().addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 255, true, false, false));
        user.sendLangMessage("command.glow.enable");
        return SUCCESS;
    }
}
