package org.kilocraft.essentials.util.commands.misc;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.CommandPermission;

import java.util.ArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HugCommand extends EssentialCommand {
    public HugCommand() {
        super("hug", CommandPermission.HUG_USE);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ArrayList<ServerPlayer> validTargets = Lists.newArrayList();
        OnlineUser src = this.getOnlineUser(ctx);

        if (src.asPlayer().totalExperience < 16 && !src.hasPermission(CommandPermission.HUG_BYPASS)) {
            src.sendLangMessage("command.hug.xp");
            return FAILED;
        }

        for (ServerPlayer player : KiloEssentials.getMinecraftServer().getPlayerList().getPlayers()) {
            OnlineUser target = this.getOnlineUser(player);

            if (!target.equals(src)) {
                validTargets.add(player);
            }
        }

        if (validTargets.isEmpty()) {
            src.sendLangError("command.hug.no_player_nearby");
            return FAILED;
        }

        ServerPlayer mainTarget = this.getClosest(validTargets, src);

        AABB eBox = new AABB(src.asPlayer().blockPosition());
        eBox = eBox.inflate(2, 2, 2);

        if (!eBox.contains(Vec3.atCenterOf(mainTarget.blockPosition()))) {
            src.sendLangMessage("command.hug.no_player_nearby");
            return FAILED;
        }

        OnlineUser onlineTarget = this.getOnlineUser(mainTarget);

        if (!src.hasPermission(CommandPermission.HUG_BYPASS)) {
            src.asPlayer().giveExperiencePoints(-16);
            mainTarget.giveExperiencePoints(8);
        }

        src.sendLangMessage("command.hug.sent", onlineTarget.getFormattedDisplayName());
        onlineTarget.sendLangMessage("command.hug.recived", src.getFormattedDisplayName());
        src.asPlayer().getLevel().playSound(
                null,
                src.asPlayer().blockPosition(),
                SoundEvents.FIREWORK_ROCKET_TWINKLE,
                SoundSource.MASTER,
                1f,
                1f
        );

        return SUCCESS;
    }

    private ServerPlayer getClosest(ArrayList<ServerPlayer> validTargets, OnlineUser src) {
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