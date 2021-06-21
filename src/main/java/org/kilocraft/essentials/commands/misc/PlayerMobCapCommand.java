package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.world.SpawnHelper;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.mixin.accessor.SpawnHelperAccessor;
import org.kilocraft.essentials.mixin.accessor.SpawnHelperInfoAccessor;
import org.kilocraft.essentials.patch.perPlayerMobSpawn.ServerPlayerEntityInterface;
import org.kilocraft.essentials.util.registry.RegistryKeyID;
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerMobCapCommand extends EssentialCommand {

    public PlayerMobCapCommand() {
        super("pmobcap", CommandPermission.PMOBCAP);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::info);
    }

    private int info(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ServerPlayerEntityInterface serverPlayerEntityInterface = (ServerPlayerEntityInterface) player;
        ServerWorld world = ctx.getSource().getWorld();
        SpawnHelper.Info spawnHelperInfo = world.getChunkManager().getSpawnInfo();
        if (spawnHelperInfo == null) KiloEssentials.getLogger().error("SpawnEntry is null");
        TextComponent.Builder text = Component.text();
        text.content("Player Mobcaps").color(NamedTextColor.YELLOW).append(Component.text(" (").color(NamedTextColor.DARK_GRAY)).append(Component.text(ServerSettings.mobcap[((RegistryKeyID) world.getRegistryKey()).getID()][0]).color(NamedTextColor.GREEN)).append(Component.text(")").color(NamedTextColor.DARK_GRAY)).append(Component.text(":\n").color(NamedTextColor.YELLOW));
        for (SpawnGroup spawnGroup : SpawnGroup.values()) {
            int count = serverPlayerEntityInterface.getMobCounts()[spawnGroup.ordinal()];
            String name = spawnGroup.getName();
            int cap = (int) (spawnGroup.getCapacity() * (ServerSettings.mobcap[((RegistryKeyID) world.getRegistryKey()).getID()][0] *
                    ServerSettings.mobcap[((RegistryKeyID) world.getRegistryKey()).getID()][spawnGroup.ordinal() + 1]));
            text.append(Component.text(name + ": ").color(NamedTextColor.GRAY)).append(Component.text(count).color(NamedTextColor.LIGHT_PURPLE)).append(Component.text("/").color(NamedTextColor.DARK_GRAY)).append(Component.text(cap).color(NamedTextColor.GOLD)).append(Component.text(" (").color(NamedTextColor.DARK_GRAY)).append(Component.text(ServerSettings.mobcap[((RegistryKeyID) world.getRegistryKey()).getID()][spawnGroup.ordinal() + 1]).color(NamedTextColor.AQUA)).append(Component.text(")\n").color(NamedTextColor.DARK_GRAY));
        }
        player.sendMessage(ComponentText.toText(text.build()), false);
        return 1;
    }

}
