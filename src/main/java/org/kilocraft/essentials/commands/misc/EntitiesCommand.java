package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntitiesCommand extends EssentialCommand {


    public EntitiesCommand() {
        super("entities", CommandPermission.ENTITIES);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("targets", EntityArgumentType.player())
                .requires(src -> hasPermission(src, CommandPermission.ENTITIES_PLAYER))
                .executes(this::executePlayer);
        argumentBuilder.executes(this::execute);
        commandNode.addChild(target.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        HashMap<ServerPlayerEntity, Integer> nearbyEntities = new HashMap<>();
        HashMap<EntityType<?>, Integer> entitiesByType = new HashMap<>();
        int entities = 0;
        int players = 0;
        for (ServerWorld world : KiloServer.getServer().getWorlds()) {
            players += world.getPlayers().size();
            for (ServerPlayerEntity player : world.getPlayers()) {
                nearbyEntities.put(player, 0);
            }
            for (Entity entity : world.iterateEntities()) {
                entitiesByType.put(entity.getType(), entitiesByType.getOrDefault(entity.getType(), 0) + 1);
                for (ServerPlayerEntity player : world.getPlayers()) {
                    int i = nearbyEntities.get(player);
                    if (entity.getChunkPos().method_24022(player.getChunkPos()) <= ServerSettings.VIEWDISTANCE.getValue() && entity.getEntityWorld().equals(player.getEntityWorld())) {
                        i++;
                    }
                    nearbyEntities.put(player, i);
                }
                entities++;
            }
        }
        TextComponent.Builder playersHover = Component.text().content("Entities / Player ").color(NamedTextColor.YELLOW)
                .append(Component.text("(").color(NamedTextColor.DARK_GRAY),
                        Component.text(entities / players + " Ø").color(NamedTextColor.GREEN),
                        Component.text(")").color(NamedTextColor.DARK_GRAY),
                        Component.text(":\n").color(NamedTextColor.YELLOW));
        sortByValue(nearbyEntities).forEach(entry -> {
            playersHover.append(ComponentText.of(entry.getKey().getEntityName() + ": ").color(NamedTextColor.GRAY))
                    .append(Component.text(entry.getValue() + "\n").color(NamedTextColor.LIGHT_PURPLE));
        });
        TextComponent.Builder entityHover = Component.text().content("Entities by Type:\n").color(NamedTextColor.YELLOW);
        int i = 0;
        HashMap<EntityType<?>, Integer> sorted = new HashMap<>();
        sortByValue(entitiesByType).forEachOrdered(entry -> sorted.put(entry.getKey(), entry.getValue()));
        for (Map.Entry<EntityType<?>, Integer> entry : sorted.entrySet()) {
            entityHover.append(Component.text(entry.getKey().getName().getString()).color(NamedTextColor.GRAY),
                    Component.text("(").color(NamedTextColor.DARK_GRAY),
                    Component.text(entry.getKey().getSpawnGroup().getName()).color(NamedTextColor.AQUA),
                    Component.text(")").color(NamedTextColor.DARK_GRAY),
                    Component.text(": ").color(NamedTextColor.GRAY),
                    Component.text(entry.getValue() + ((i % 3 == 2) ? "\n" : " ")).color(NamedTextColor.LIGHT_PURPLE));
            i++;
        }
        TextComponent.Builder builder = Component.text().content("There are currently ").color(NamedTextColor.YELLOW)
                .append(Component.text(entities).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(entityHover.build())),
                        Component.text(" loaded, by ").color(NamedTextColor.YELLOW),
                        Component.text(players).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(playersHover.build())),
                        Component.text(" players.").color(NamedTextColor.YELLOW));

        getCommandSource(ctx).sendMessage(builder.build());
        return entities;
    }

    private int executePlayer(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayers(ctx, "targets").iterator().next();
        HashMap<EntityType<?>, Integer> entitiesByType = new HashMap<>();
        int entities = 0;
        for (Entity entity : player.getServerWorld().iterateEntities()) {
            if (entity.getChunkPos().method_24022(player.getChunkPos()) <= ServerSettings.VIEWDISTANCE.getValue()) {
                entitiesByType.put(entity.getType(), entitiesByType.getOrDefault(entity.getType(), 0) + 1);
                entities++;
            }
        }
        TextComponent.Builder entityHover = Component.text().content("Entities by Type:\n").color(NamedTextColor.YELLOW);
        sortByValue(entitiesByType).forEach(entry -> {
            entityHover.append(Component.text(entry.getKey().getName().getString()).color(NamedTextColor.GRAY),
                    Component.text("(").color(NamedTextColor.DARK_GRAY),
                    Component.text(entry.getKey().getSpawnGroup().getName()).color(NamedTextColor.AQUA),
                    Component.text(")").color(NamedTextColor.DARK_GRAY),
                    Component.text(": ").color(NamedTextColor.GRAY),
                    Component.text(entry.getValue() + "\n").color(NamedTextColor.LIGHT_PURPLE));
        });

        TextComponent.Builder builder = Component.text().content(player.getEntityName()).color(NamedTextColor.GOLD)
                .append(Component.text(" currently loads ").color(NamedTextColor.YELLOW),
                        Component.text(entities).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(entityHover.build())),
                        Component.text(".").color(NamedTextColor.YELLOW));
        getCommandSource(ctx).sendMessage(builder.build());
        return entities;
    }


    private <K> Stream<? extends Map.Entry<K, Integer>> sortByValue(Map<K, Integer> unsortMap) {
        return unsortMap.entrySet().stream().sorted((k1, k2) -> -k1.getValue().compareTo(k2.getValue()));
    }
}
