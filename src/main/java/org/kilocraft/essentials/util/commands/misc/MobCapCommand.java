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
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.mixin.patch.performance.perPlayerMobcap.SpawnHelperAccessor;
import org.kilocraft.essentials.mixin.patch.performance.perPlayerMobcap.SpawnHelperInfoAccessor;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.registry.RegistryKeyID;
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
        RequiredArgumentBuilder<ServerCommandSource, Float> multiplier = this.argument("multiplier", FloatArgumentType.floatArg(0, 100));
        multiplier.executes(ctx -> this.execute(ctx, DimensionArgumentType.getDimensionArgument(ctx, "dimension")));
        final RequiredArgumentBuilder<ServerCommandSource, String> spawnGroup = this.argument("name", StringArgumentType.word())
                .suggests(MobCapCommand::suggestSpawnGroups)
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.MOBCAP_SET));
        final RequiredArgumentBuilder<ServerCommandSource, Identifier> world = this.argument("dimension", DimensionArgumentType.dimension());
        world.executes(ctx -> this.info(ctx, DimensionArgumentType.getDimensionArgument(ctx, "dimension")));
        spawnGroup.then(multiplier);
        world.then(spawnGroup);
        this.argumentBuilder.executes(ctx -> this.info(ctx, ctx.getSource().getWorld()));
        this.commandNode.addChild(world.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx, ServerWorld world) throws CommandSyntaxException {
        float f = FloatArgumentType.getFloat(ctx, "multiplier");
        String name = StringArgumentType.getString(ctx, "name");
        Optional<SpawnGroup> spawnGroup = Optional.ofNullable(SpawnGroup.byName(name));
        Identifier id = world.getRegistryKey().getValue();
        if (spawnGroup.isPresent()) {
            ServerSettings.setFloat("mobcap." + id.getPath() + "." + spawnGroup.get().getName().toLowerCase(), f);
        } else if (name.equals("global")) {
            ServerSettings.setFloat("mobcap." + id.getPath(), f);
        } else {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid spawn group: " + name)).create();
        }
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        player.sendMessage(StringText.of("command.mobpsawn", f, name), false);
        return SUCCESS;
    }

    private int info(CommandContext<ServerCommandSource> ctx, ServerWorld world) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        SpawnHelper.Info spawnHelperInfo = world.getChunkManager().getSpawnInfo();
        if (spawnHelperInfo == null) KiloEssentials.getLogger().error("SpawnEntry is null");
        TextComponent.Builder text = Component.text();
        text.content("Mobcaps").color(NamedTextColor.YELLOW)
                .append(Component.text(" (").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(String.format("%.1f", ServerSettings.tick_utils_global_mobcap)).color(NamedTextColor.RED))
                .append(Component.text(", ").color(NamedTextColor.GRAY))
                .append(Component.text(ServerSettings.mobcap[((RegistryKeyID) world.getRegistryKey()).getID()][0]).color(NamedTextColor.GREEN))
                .append(Component.text(")").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(":\n").color(NamedTextColor.YELLOW));
        for (SpawnGroup spawnGroup : SpawnGroup.values()) {
            int count = spawnHelperInfo.getGroupToCount().getOrDefault(spawnGroup, 0);
            int cap = spawnGroup.getCapacity() * ((SpawnHelperInfoAccessor) spawnHelperInfo).getSpawnChunkCount() / SpawnHelperAccessor.getChunkArea();
            cap *= ServerSettings.tick_utils_global_mobcap * ServerSettings.mobcap[((RegistryKeyID) world.getRegistryKey()).getID()][0] * ServerSettings.mobcap[((RegistryKeyID) world.getRegistryKey()).getID()][spawnGroup.ordinal() + 1];
            String name = spawnGroup.getName();
            text.append(Component.text(name + ": ").color(NamedTextColor.GRAY)).append(Component.text(count).color(NamedTextColor.LIGHT_PURPLE)).append(Component.text("/").color(NamedTextColor.DARK_GRAY)).append(Component.text(cap).color(NamedTextColor.GOLD)).append(Component.text(" (").color(NamedTextColor.DARK_GRAY)).append(Component.text(ServerSettings.mobcap[((RegistryKeyID) world.getRegistryKey()).getID()][spawnGroup.ordinal() + 1]).color(NamedTextColor.AQUA)).append(Component.text(")\n").color(NamedTextColor.DARK_GRAY));
        }
        player.sendMessage(ComponentText.toText(text.build()), false);
        return SUCCESS;
    }

}
