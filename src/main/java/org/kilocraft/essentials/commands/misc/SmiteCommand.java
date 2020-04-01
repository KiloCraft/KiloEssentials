package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.chat.LangText;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.util.EntityServerRayTraceable;

public class SmiteCommand extends EssentialCommand {
    public SmiteCommand() {
        super("smite", CommandPermission.SMITE, new String[]{"lightning"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        Vec3d vec3d = ((EntityServerRayTraceable) player).rayTrace(90.0D, 1.0F, true).getPos();
        LightningEntity lightning = new LightningEntity(player.getServerWorld(), vec3d.x, vec3d.y, vec3d.z, false);
        player.getServerWorld().addLightning(lightning);

        player.addMessage(LangText.getFormatter(true, "command.smite"), true);
        return SINGLE_SUCCESS;
    }

}
