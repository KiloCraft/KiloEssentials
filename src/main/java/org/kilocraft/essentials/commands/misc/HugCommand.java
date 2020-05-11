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
        ctx.getSource().getMinecraftServer().getPlayerManager().getPlayerList().forEach(player -> {
            OnlineUser target = getOnlineUser(player);
            Box Ebox = new Box(src.asPlayer().getBlockPos());
            Ebox = Ebox.expand(2, 2, 2);
            if (Ebox.contains(Vec3d.ofCenter(player.getBlockPos()))){
                if(!target.equals(src)) {
                    target.sendLangMessage("command.hug.message", src.getFormattedDisplayName());
                    target.asPlayer().playSound(
                            SoundEvents.ENTITY_VILLAGER_CELEBRATE,
                            SoundCategory.MASTER,
                            1,
                            1);
                }
            } else {
                src.sendLangMessage("command.hug.notclose");
            }
        });
        return 1;
    }
}