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

import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.ArrayList;

public class HugCommand extends EssentialCommand {
    public HugCommand() {
        super("hug", CommandPermission.HUG_USE);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ArrayList<ServerPlayerEntity> validTargets = new ArrayList<>();
        OnlineUser src = this.getOnlineUser(ctx);

        if (src.asPlayer().totalExperience < 16 && !src.hasPermission(CommandPermission.HUG_BYPASS)) {
            src.sendLangMessage("command.hug.xp");
            return FAILED;
        }

        for (ServerPlayerEntity player : this.server.getPlayerManager().getPlayerList()) {
            OnlineUser target = getOnlineUser(player);

            if (!target.equals(src)) {
                validTargets.add(player);
            }
        }

        if (validTargets.isEmpty()) {
            src.sendLangError("command.hug.no_player_nearby");
            return FAILED;
        }

        ServerPlayerEntity mainTarget = getClosest(validTargets, src);

        Box eBox = new Box(src.asPlayer().getBlockPos());
        eBox = eBox.expand(2, 2, 2);

        if (!eBox.contains(Vec3d.ofCenter(mainTarget.getBlockPos()))) {
            src.sendLangMessage("command.hug.no_player_nearby");
            return FAILED;
        }

        OnlineUser onlineTarget  = getOnlineUser(mainTarget);

        if(!src.hasPermission(CommandPermission.HUG_BYPASS)) {
            src.asPlayer().addExperience(-16);
            mainTarget.addExperience(8);
        }

        src.sendLangMessage("command.hug.sent", onlineTarget.getFormattedDisplayName());
        onlineTarget.sendLangMessage("command.hug.recived", src.getFormattedDisplayName());
        src.asPlayer().getEntityWorld().playSound(
                null,
                src.asPlayer().getBlockPos(),
                SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE,
                SoundCategory.MASTER,
                1f,
                1f
        );

        return SUCCESS;
    }

    private ServerPlayerEntity getClosest(ArrayList<ServerPlayerEntity> validTargets, OnlineUser src) {
        double srcLocation = src.asPlayer().getX();
        double distance = Math.abs(validTargets.get(0).getX() - srcLocation);

        int id = 0;

        for (int c = 1; c < validTargets.size(); c++) {
            double cDistance = Math.abs(validTargets.get(c).getX() - srcLocation);
            if (cDistance < distance) {
                id = c;
                distance = cDistance;
            }
        }

        return validTargets.get(id);
    }
}