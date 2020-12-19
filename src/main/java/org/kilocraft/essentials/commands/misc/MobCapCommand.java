package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.mixin.accessor.SpawnHelperAccessor;
import org.kilocraft.essentials.mixin.accessor.SpawnHelperInfoAccessor;

public class MobCapCommand extends EssentialCommand {

    public static float mult = 1;

    public MobCapCommand() {
        super("mobcap", CommandPermission.MOBCAP);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, Float> multiplier = argument("multiplier", FloatArgumentType.floatArg(0, 100));
        multiplier.executes(this::execute);
        argumentBuilder.executes(this::info);
        commandNode.addChild(multiplier.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Float f = FloatArgumentType.getFloat(ctx, "multiplier");
        mult = f;
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        player.sendMessage(StringText.of(true, "command.mobpsawn", f), false);
        return SUCCESS;
    }

    private int info(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ServerWorld world = ctx.getSource().getWorld();
        SpawnHelper.Info spawnHelperInfo = world.getChunkManager().getSpawnInfo();
        if (spawnHelperInfo == null) KiloEssentials.getLogger().error("SpawnEntry is null");
        TextComponent.Builder text = Component.text();
        text.content("Mobcaps (" + mult + "):\n").color(NamedTextColor.YELLOW);
        for (SpawnGroup spawnGroup : SpawnGroup.values()) {
            String name = spawnGroup.getName();
            int cap = (int) ((spawnGroup.getCapacity() * ((SpawnHelperInfoAccessor) spawnHelperInfo).getSpawnChunkCount() / SpawnHelperAccessor.getChunkArea()) * MobCapCommand.mult);
            text.append(Component.text(name + ": ").color(NamedTextColor.GRAY)).append(Component.text(cap + "\n").color(NamedTextColor.GOLD));
        }
        player.sendMessage(ComponentText.toText(text.build()), false);
        return SUCCESS;
    }

}
