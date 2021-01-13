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
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MobCapCommand extends EssentialCommand {

    public MobCapCommand() {
        super("mobcap", CommandPermission.MOBCAP_QUERY);
    }

    public static CompletableFuture<Suggestions> suggestSpawnGroups(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> list = Arrays.stream(SpawnGroup.values()).map(SpawnGroup::getName).collect(Collectors.toList());
        list.add("global");
        return CommandSource.suggestMatching(list, builder);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, Float> multiplier = argument("multiplier", FloatArgumentType.floatArg(0, 100));
        multiplier.executes(ctx -> execute(ctx, DimensionArgumentType.getDimensionArgument(ctx, "dimension")));
        final RequiredArgumentBuilder<ServerCommandSource, String> spawnGroup = this.argument("name", StringArgumentType.word())
                .suggests(MobCapCommand::suggestSpawnGroups)
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.MOBCAP_SET));
        final RequiredArgumentBuilder<ServerCommandSource, Identifier> world = argument("dimension", DimensionArgumentType.dimension());
        world.executes(ctx -> info(ctx, DimensionArgumentType.getDimensionArgument(ctx, "dimension")));
        spawnGroup.then(multiplier);
        world.then(spawnGroup);
        argumentBuilder.executes(ctx -> info(ctx, ctx.getSource().getWorld()));
        commandNode.addChild(world.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx, ServerWorld world) throws CommandSyntaxException {
        float f = FloatArgumentType.getFloat(ctx, "multiplier");
        String name = StringArgumentType.getString(ctx, "name");
        Optional<SpawnGroup> spawnGroup = Optional.ofNullable(SpawnGroup.byName(name));
        Identifier id = world.getRegistryKey().getValue();
        if (spawnGroup.isPresent()) {
            KiloEssentials.getInstance().getSettingManager().setMultiplier(id, spawnGroup.get(), f);
        } else if (name.equals("global")) {
            KiloEssentials.getInstance().getSettingManager().setMultiplier(id, null, f);
        } else {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid spawn group: " + name)).create();
        }
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        player.sendMessage(StringText.of(true, "command.mobpsawn", f, name), false);
        return SUCCESS;
    }

    private int info(CommandContext<ServerCommandSource> ctx, ServerWorld world) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        SpawnHelper.Info spawnHelperInfo = world.getChunkManager().getSpawnInfo();
        ServerSettings settingManager = KiloEssentials.getInstance().getSettingManager();
        if (spawnHelperInfo == null) KiloEssentials.getLogger().error("SpawnEntry is null");
        TextComponent.Builder text = Component.text();
        text.content("Mobcaps").color(NamedTextColor.YELLOW).append(Component.text(" (").color(NamedTextColor.DARK_GRAY)).append(Component.text(settingManager.getMultiplier(world.getRegistryKey().getValue(), null)).color(NamedTextColor.GREEN)).append(Component.text(")").color(NamedTextColor.DARK_GRAY)).append(Component.text(":\n").color(NamedTextColor.YELLOW));
        for (SpawnGroup spawnGroup : SpawnGroup.values()) {
            int count = spawnHelperInfo.getGroupToCount().getOrDefault(spawnGroup, 0);
            String name = spawnGroup.getName();
            int cap = (int) ((spawnGroup.getCapacity() * ((SpawnHelperInfoAccessor) spawnHelperInfo).getSpawnChunkCount() / SpawnHelperAccessor.getChunkArea()) * settingManager.getMultiplier(world.getRegistryKey().getValue(), spawnGroup));
            text.append(Component.text(name + ": ").color(NamedTextColor.GRAY)).append(Component.text(count).color(NamedTextColor.LIGHT_PURPLE)).append(Component.text("/").color(NamedTextColor.DARK_GRAY)).append(Component.text(cap).color(NamedTextColor.GOLD)).append(Component.text(" (").color(NamedTextColor.DARK_GRAY)).append(Component.text(settingManager.getMultiplier(world.getRegistryKey().getValue(), spawnGroup)).color(NamedTextColor.AQUA)).append(Component.text(")\n").color(NamedTextColor.DARK_GRAY));
        }
        player.sendMessage(ComponentText.toText(text.build()), false);
        return (int) (settingManager.getMultiplier(world.getRegistryKey().getValue(), null) * 100);
    }

}
