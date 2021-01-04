package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.StringText;

public class ShulkerSpawnChanceCommand extends EssentialCommand {

    public ShulkerSpawnChanceCommand() {
        super("shulkerchance", CommandPermission.SHULKER_CHANCE);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, Double> chance = argument("chance", DoubleArgumentType.doubleArg(0, 100));
        chance.executes(this::execute);
        argumentBuilder.executes(this::info);
        commandNode.addChild(chance.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        double chance = DoubleArgumentType.getDouble(ctx, "chance");
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        KiloEssentials.getInstance().getSettingManager().setShulkerSpawnChance(chance);
        player.sendMessage(StringText.of(true, "command.shulkerchance.set", chance), false);
        return SUCCESS;
    }

    private int info(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        player.sendMessage(StringText.of(true, "command.shulkerchance.info", KiloEssentials.getInstance().getSettingManager().getShulkerSpawnChance()), false);
        return SUCCESS;
    }
}
