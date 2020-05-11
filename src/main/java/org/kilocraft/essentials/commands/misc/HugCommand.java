package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;


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
        //Get all players and testing them for the statements.
        ctx.getSource().getMinecraftServer().getPlayerManager().getPlayerList().forEach(player -> {
            OnlineUser target = getOnlineUser(player);
            //Creating a box.
            Box Ebox = new Box(src.asPlayer().getBlockPos());
            Ebox = Ebox.expand(2, 2, 2);
            //Check if the players block position is contained by the box.
            if (Ebox.contains(Vec3d.ofCenter(player.getBlockPos()))){
                //If the target is the src. Dont run.
                if(!target.equals(src)) {
                    //If the player src has enough total experience.
                    if(src.asPlayer().totalExperience >= 16) {
                        //Experience handling.
                        src.asPlayer().addExperience(-16);
                        target.asPlayer().addExperience(8);
                        //Message handler.
                        target.sendLangMessage("command.hug.message", src.getFormattedDisplayName());
                        //Sound handler.
                        target.asPlayer().playSound(
                                SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE,
                                SoundCategory.MASTER,
                                1,
                                1);
                    } else {
                        src.sendLangMessage("command.hug.xp");
                    }
                }
            } else {
                src.sendLangMessage("command.hug.notclose");
            }
        });
        return 1;
    }
}