package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EntitiesCommand extends EssentialCommand {

    public EntitiesCommand() {
        super("entities", CommandPermission.ENTITIES);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        LiteralArgumentBuilder<ServerCommandSource> byPlayer = LiteralArgumentBuilder.literal("byPlayer");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> player = CommandManager.argument("player", EntityArgumentType.player())
                .requires(src -> hasPermission(src, CommandPermission.ENTITIES_PLAYER))
                .executes(this::executePlayer);

        LiteralArgumentBuilder<ServerCommandSource> byType = LiteralArgumentBuilder.literal("byType");
        RequiredArgumentBuilder<ServerCommandSource, Identifier> type = CommandManager.argument("type", EntitySummonArgumentType.entitySummon())
                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                .requires(src -> hasPermission(src, CommandPermission.ENTITIES_TYPE))
                .executes(this::executeType);

        LiteralArgumentBuilder<ServerCommandSource> items = LiteralArgumentBuilder.literal("items");
        items.requires(src -> hasPermission(src, CommandPermission.ENTITIES_ITEM)).executes(this::executeItem);

        argumentBuilder.executes(this::execute);

        byPlayer.then(player);
        byType.then(type);
        commandNode.addChild(byType.build());
        commandNode.addChild(byPlayer.build());
        commandNode.addChild(items.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        HashMap<ServerPlayerEntity, Integer> nearbyEntities = new HashMap<>();
        HashMap<EntityType<?>, Integer> entitiesByType = new HashMap<>();
        int entities = 0;
        int players = 0;
        for (ServerWorld world : KiloEssentials.getMinecraftServer().getWorlds()) {
            players += world.getPlayers().size();
            for (Entity entity : world.iterateEntities()) {
                entitiesByType.put(entity.getType(), entitiesByType.getOrDefault(entity.getType(), 0) + 1);
                for (ServerPlayerEntity player : world.getPlayers()) {
                    int i = nearbyEntities.getOrDefault(player, 0);
                    if (entity.getChunkPos().getChebyshevDistance(player.getChunkPos()) <= ServerSettings.getInt("view_distance") && entity.getEntityWorld().equals(player.getEntityWorld())) {
                        i++;
                    }
                    nearbyEntities.put(player, i);
                }
                entities++;
            }
        }
        TextComponent.Builder playersHover = Component.text().content("Entities / Player ").color(NamedTextColor.YELLOW)
                .append(Component.text(":\n").color(NamedTextColor.YELLOW));
        sortByValue(nearbyEntities).forEach(entry -> {
            playersHover.append(ComponentText.of(entry.getKey().getEntityName() + ": ").color(NamedTextColor.GRAY))
                    .append(Component.text(entry.getValue() + "\n").color(NamedTextColor.LIGHT_PURPLE));
        });
        TextComponent.Builder entityHover = Component.text().content("Entities by Type:\n").color(NamedTextColor.YELLOW);
        final int[] i = {0};
        sortByValue(entitiesByType).forEachOrdered(entry -> {
            entityHover.append(Component.text(entry.getKey().getName().getString()).color(NamedTextColor.GRAY),
                    Component.text("(").color(NamedTextColor.DARK_GRAY),
                    Component.text(entry.getKey().getSpawnGroup().getName()).color(NamedTextColor.AQUA),
                    Component.text(")").color(NamedTextColor.DARK_GRAY),
                    Component.text(": ").color(NamedTextColor.GRAY),
                    ComponentText.of("<color:" + getHex(entry.getValue(), 1000) + ">" + entry.getValue() + ((i[0] % 3 == 2) ? "\n" : " ")));
            i[0]++;
        });
        TextComponent.Builder builder = Component.text().content("There are currently ").color(NamedTextColor.YELLOW)
                .append(Component.text(entities).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(entityHover.build())),
                        Component.text(" entities loaded, by ").color(NamedTextColor.YELLOW),
                        Component.text(players).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(playersHover.build())),
                        Component.text(" players.").color(NamedTextColor.YELLOW));

        getCommandSource(ctx).sendMessage(builder.build());
        return entities;
    }

    private int executeItem(CommandContext<ServerCommandSource> ctx) {
        HashMap<Item, Integer> items = new HashMap<>();
        int itemCount = 0;
        for (ServerWorld world : KiloEssentials.getMinecraftServer().getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (!(entity instanceof ItemEntity)) continue;
                ItemEntity itemEntity = (ItemEntity) entity;
                Item item = itemEntity.getStack().getItem();
                items.put(item, items.getOrDefault(item, 0) + 1);
                itemCount++;
            }
        }
        TextComponent.Builder itemHover = Component.text().content("Items").color(NamedTextColor.YELLOW)
                .append(Component.text(":\n").color(NamedTextColor.YELLOW));
        final int[] i = {0};
        sortByValue(items).forEach(entry -> {
            itemHover.append(ComponentText.toComponent(new TranslatableText(entry.getKey().getTranslationKey())).color(NamedTextColor.GRAY), ComponentText.of(": ").color(NamedTextColor.GRAY))
                    .append(ComponentText.of("<color:" + getHex(entry.getValue(), 500) + ">" + entry.getValue() + ((i[0] % 3 == 2) ? "\n" : " ")));
            i[0]++;
        });
        TextComponent.Builder builder = Component.text().content("There are currently ").color(NamedTextColor.YELLOW)
                .append(Component.text(itemCount).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(itemHover.build())),
                        Component.text(" items loaded.").color(NamedTextColor.YELLOW));

        getCommandSource(ctx).sendMessage(builder.build());
        return itemCount;
    }

    private int executePlayer(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayers(ctx, "player").iterator().next();
        HashMap<EntityType<?>, Integer> entitiesByType = new HashMap<>();
        int entities = 0;
        for (Entity entity : player.getServerWorld().iterateEntities()) {
            if (entity.getChunkPos().getChebyshevDistance(player.getChunkPos()) <= ServerSettings.getInt("view_distance")) {
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
                        Component.text(" entities.").color(NamedTextColor.YELLOW));
        getCommandSource(ctx).sendMessage(builder.build());
        return entities;
    }

    private int executeType(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Identifier filter = EntitySummonArgumentType.getEntitySummon(ctx, "type");
        HashMap<ServerPlayerEntity, Integer> nearbyEntities = new HashMap<>();
        int entities = 0;
        int players = 0;
        EntityType<?> entityType = Registry.ENTITY_TYPE.get(filter);
        for (ServerWorld world : KiloEssentials.getMinecraftServer().getWorlds()) {
            players += world.getPlayers().size();
            for (Entity entity : world.iterateEntities()) {
                if (!entity.getType().equals(entityType)) continue;
                for (ServerPlayerEntity player : world.getPlayers()) {
                    int i = nearbyEntities.getOrDefault(player, 0);
                    if (entity.getChunkPos().getChebyshevDistance(player.getChunkPos()) <= ServerSettings.getInt("view_distance") && entity.getEntityWorld().equals(player.getEntityWorld())) {
                        i++;
                    }
                    nearbyEntities.put(player, i);
                }
                entities++;
            }
        }
        TextComponent.Builder playersHover = Component.text()
                .append(ComponentText.toComponent(entityType.getName()).color(NamedTextColor.YELLOW), Component.text("s / Player :\n").color(NamedTextColor.YELLOW));
        sortByValue(nearbyEntities).forEach(entry -> {
            playersHover.append(ComponentText.of(entry.getKey().getEntityName() + ": ").color(NamedTextColor.GRAY))
                    .append(Component.text(entry.getValue() + "\n").color(NamedTextColor.LIGHT_PURPLE));
        });
        TextComponent.Builder builder = Component.text().content("There are currently ").color(NamedTextColor.YELLOW)
                .append(Component.text(entities + " ").color(NamedTextColor.GOLD),
                        ComponentText.toComponent(entityType.getName()).color(NamedTextColor.GOLD),
                        Component.text(" loaded, by ").color(NamedTextColor.YELLOW),
                        Component.text(players).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(playersHover.build())),
                        Component.text(" players.").color(NamedTextColor.YELLOW));

        getCommandSource(ctx).sendMessage(builder.build());
        return entities;
    }

    private <K> Stream<? extends Map.Entry<K, Integer>> sortByValue(Map<K, Integer> unsortMap) {
        return unsortMap.entrySet().stream().sorted((k1, k2) -> -k1.getValue().compareTo(k2.getValue()));
    }

    public String getHex(int value, int max) {
        int green = 255, red = 0, blue = 0;
        double v = value;
        for (int i = 0; i < (v / max) * 255; i++) {
            if (green > 0 && blue == 0) {
                green--;
                red++;
            }
            if (red > 0 && green == 0) {
                red--;
                blue++;
            }
            if (blue > 0 && red == 0) {
                green++;
                blue--;
            }
        }
        return String.format("#%02X%02X%02X", red, green, blue);
    }


    /*var r=255,g=0,b=0;

    setInterval(function(){
        if(r > 0 && b == 0){
            r--;
            g++;
        }
        if(g > 0 && r == 0){
            g--;
            b++;
        }
        if(b > 0 && g == 0){
            r++;
            b--;
        }
        $("#color").text("rgb("+r+","+g+","+b+")");
        $("#color").css("color","rgb("+r+","+g+","+b+")");
    },10);*/
}
