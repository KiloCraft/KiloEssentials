package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.ArrayList;
import java.util.List;


public class HugCommand extends EssentialCommand {
    public HugCommand() {
        super("hug");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder
                .executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        for (ServerPlayerEntity player : this.server.getPlayerManager().getPlayerList()) {
            OnlineUser target = getOnlineUser(player);
            Box eBox = new Box(src.asPlayer().getBlockPos());
            eBox = eBox.expand(2, 2, 2);

            if (!target.equals(src)) {

                if (!eBox.contains(Vec3d.ofCenter(player.getBlockPos()))) {
                    return FAILED;
                }

                if (src.asPlayer().totalExperience < 16) {
                    src.sendLangMessage("command.hug.xp");
                    return FAILED;
                }

                src.asPlayer().addExperience(-16);
                player.addExperience(8);

                target.sendLangMessage("command.hug.message", src.getFormattedDisplayName());

                player.playSound(
                        SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE,
                        SoundCategory.MASTER,
                        1,
                        1
                );
            }
            return SUCCESS;
        }
        return SUCCESS;
    }
}