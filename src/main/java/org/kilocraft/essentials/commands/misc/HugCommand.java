package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.LangText;
import org.kilocraft.essentials.chat.ServerChat;
import sun.awt.windows.WPrinterJob;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.command.arguments.EntityArgumentType.*;

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
        ctx.getSource().getMinecraftServer().getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
            OnlineUser target = getOnlineUser(serverPlayerEntity);
            Box Ebox = new Box(src.asPlayer().getBlockPos());
            Ebox = Ebox.expand(2, 2, 2);
            if (Ebox.contains(Vec3d.ofCenter(target.asPlayer().getBlockPos()))){
                if(target.equals(src)) {
                    return;
                }
                target.sendLangMessage("command.hug.message", src.getDisplayName());
                target.asPlayer().playSound(
                        SoundEvents.ENTITY_VILLAGER_CELEBRATE,
                        SoundCategory.MASTER,
                        1,
                        1);
            } else {
                src.sendLangMessage("command.hug.notclose");
            }



        });

        return 1;
    }
}