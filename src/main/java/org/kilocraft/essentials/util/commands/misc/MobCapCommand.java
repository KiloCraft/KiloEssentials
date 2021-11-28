package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.patch.SpawnUtil;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MobCapCommand extends EssentialCommand {

    public MobCapCommand() {
        super("mobcap", CommandPermission.MOBCAP_QUERY);
    }

    public static CompletableFuture<Suggestions> suggestSpawnGroups(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> list = Arrays.stream(MobCategory.values()).map(MobCategory::getName).collect(Collectors.toList());
        list.add("global");
        return SharedSuggestionProvider.suggest(list, builder);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, Float> multiplier = this.argument("multiplier", FloatArgumentType.floatArg(0, 100));
        multiplier.executes(ctx -> this.execute(ctx, DimensionArgument.getDimension(ctx, "dimension")));
        final RequiredArgumentBuilder<CommandSourceStack, String> spawnGroup = this.argument("name", StringArgumentType.word())
                .suggests(MobCapCommand::suggestSpawnGroups)
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.MOBCAP_SET));
        final RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> world = this.argument("dimension", DimensionArgument.dimension());
        world.executes(ctx -> this.info(ctx, DimensionArgument.getDimension(ctx, "dimension")));
        spawnGroup.then(multiplier);
        world.then(spawnGroup);
        this.argumentBuilder.executes(ctx -> this.info(ctx, ctx.getSource().getLevel()));
        this.commandNode.addChild(world.build());
    }

    private int execute(CommandContext<CommandSourceStack> ctx, ServerLevel world) throws CommandSyntaxException {
        float f = FloatArgumentType.getFloat(ctx, "multiplier");
        String name = StringArgumentType.getString(ctx, "name");
        Optional<MobCategory> spawnGroup = Optional.ofNullable(MobCategory.byName(name));
        ResourceLocation id = world.dimension().location();
        if (spawnGroup.isPresent()) {
            ServerSettings.setFloat("mobcap." + id.getPath() + "." + spawnGroup.get().getName().toLowerCase(), f);
        } else if (name.equals("global")) {
            ServerSettings.setFloat("mobcap." + id.getPath(), f);
        } else {
            throw new SimpleCommandExceptionType(new net.minecraft.network.chat.TextComponent("Invalid spawn group: " + name)).create();
        }
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        player.displayClientMessage(StringText.of("command.mobpsawn", f, name), false);
        return SUCCESS;
    }

    private int info(CommandContext<CommandSourceStack> ctx, ServerLevel world) throws CommandSyntaxException {
        NaturalSpawner.SpawnState info = world.getChunkSource().getLastSpawnState();
        Objects.requireNonNull(info, "SpawnHelper.Info must not be null");
        sendMobCap(ctx.getSource().getPlayerOrException(), world, "Global MobCap", info.getMobCategoryCounts(), group -> SpawnUtil.getGlobalMobCap(info, world, group));
        return SUCCESS;
    }

    public static void sendMobCap(ServerPlayer player, ServerLevel world, String title, Object2IntMap<MobCategory> spawnGroupCounts, Function<MobCategory, Integer> getSpawnGroupMobCap) {
        TextComponent.Builder text = Component.text();
        text.content(title).color(NamedTextColor.YELLOW)
                .append(Component.text(" (").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(SpawnUtil.getMobCapMultiplier(world, 0)).color(NamedTextColor.GREEN))
                .append(Component.text(")").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(":\n").color(NamedTextColor.YELLOW));
        for (MobCategory group : MobCategory.values()) {
            int count = spawnGroupCounts.getOrDefault(group, 0);
            int cap = getSpawnGroupMobCap.apply(group);
            String name = group.getName();
            text.append(Component.text(name + ": ").color(NamedTextColor.GRAY))
                    .append(Component.text(count).color(NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text("/").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text(cap).color(NamedTextColor.GOLD))
                    .append(Component.text(" (").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text(SpawnUtil.getMobCapMultiplier(world, group)).color(NamedTextColor.AQUA))
                    .append(Component.text(")\n").color(NamedTextColor.DARK_GRAY));
        }
        player.displayClientMessage(ComponentText.toText(text.build()), false);
    }

}
