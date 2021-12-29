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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EntitiesCommand extends EssentialCommand {

    public EntitiesCommand() {
        super("entities", CommandPermission.ENTITIES);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        LiteralArgumentBuilder<CommandSourceStack> byPlayer = LiteralArgumentBuilder.literal("byPlayer");
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> player = Commands.argument("player", EntityArgument.player())
                .requires(src -> this.hasPermission(src, CommandPermission.ENTITIES_PLAYER))
                .executes(this::executePlayer);

        LiteralArgumentBuilder<CommandSourceStack> byType = LiteralArgumentBuilder.literal("byType");
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> type = Commands.argument("type", EntitySummonArgument.id())
                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                .requires(src -> this.hasPermission(src, CommandPermission.ENTITIES_TYPE))
                .executes(this::executeType);

        LiteralArgumentBuilder<CommandSourceStack> items = LiteralArgumentBuilder.literal("items");
        items.requires(src -> this.hasPermission(src, CommandPermission.ENTITIES_ITEM)).executes(this::executeItem);

        this.argumentBuilder.executes(this::execute);

        byPlayer.then(player);
        byType.then(type);
        this.commandNode.addChild(byType.build());
        this.commandNode.addChild(byPlayer.build());
        this.commandNode.addChild(items.build());
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        HashMap<ServerPlayer, Integer> nearbyEntities = new HashMap<>();
        HashMap<EntityType<?>, Integer> entitiesByType = new HashMap<>();
        int entities = 0;
        int players = 0;
        for (ServerLevel world : KiloEssentials.getMinecraftServer().getAllLevels()) {
            players += world.players().size();
            for (Entity entity : world.getAllEntities()) {
                entitiesByType.put(entity.getType(), entitiesByType.getOrDefault(entity.getType(), 0) + 1);
                for (ServerPlayer player : world.players()) {
                    int i = nearbyEntities.getOrDefault(player, 0);
                    if (entity.chunkPosition().getChessboardDistance(player.chunkPosition()) <= ServerSettings.getViewDistance() && entity.getCommandSenderWorld().equals(player.getCommandSenderWorld())) {
                        i++;
                    }
                    nearbyEntities.put(player, i);
                }
                entities++;
            }
        }
        TextComponent.Builder playersHover = Component.text().content("Entities / Player ").color(NamedTextColor.YELLOW)
                .append(Component.text(":\n").color(NamedTextColor.YELLOW));
        this.sortByValue(nearbyEntities).forEach(entry -> {
            playersHover.append(ComponentText.of(entry.getKey().getScoreboardName() + ": ").color(NamedTextColor.GRAY))
                    .append(Component.text(entry.getValue() + "\n").color(NamedTextColor.LIGHT_PURPLE));
        });
        TextComponent.Builder entityHover = Component.text().content("Entities by Type:\n").color(NamedTextColor.YELLOW);
        final int[] i = {0};
        this.sortByValue(entitiesByType).forEachOrdered(entry -> {
            entityHover.append(Component.text(entry.getKey().getDescription().getString()).color(NamedTextColor.GRAY),
                    Component.text("(").color(NamedTextColor.DARK_GRAY),
                    Component.text(entry.getKey().getCategory().getName()).color(NamedTextColor.AQUA),
                    Component.text(")").color(NamedTextColor.DARK_GRAY),
                    Component.text(": ").color(NamedTextColor.GRAY),
                    ComponentText.of("<color:" + this.getHex(entry.getValue(), 1000) + ">" + entry.getValue() + ((i[0] % 3 == 2) ? "\n" : " ")));
            i[0]++;
        });
        TextComponent.Builder builder = Component.text().content("There are currently ").color(NamedTextColor.YELLOW)
                .append(Component.text(entities).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(entityHover.build())),
                        Component.text(" entities loaded, by ").color(NamedTextColor.YELLOW),
                        Component.text(players).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(playersHover.build())),
                        Component.text(" players.").color(NamedTextColor.YELLOW));

        this.getCommandSource(ctx).sendMessage(builder.build());
        return entities;
    }

    private int executeItem(CommandContext<CommandSourceStack> ctx) {
        HashMap<Item, Integer> items = new HashMap<>();
        int itemCount = 0;
        for (ServerLevel world : KiloEssentials.getMinecraftServer().getAllLevels()) {
            for (Entity entity : world.getAllEntities()) {
                if (!(entity instanceof ItemEntity)) continue;
                ItemEntity itemEntity = (ItemEntity) entity;
                Item item = itemEntity.getItem().getItem();
                items.put(item, items.getOrDefault(item, 0) + 1);
                itemCount++;
            }
        }
        TextComponent.Builder itemHover = Component.text().content("Items").color(NamedTextColor.YELLOW)
                .append(Component.text(":\n").color(NamedTextColor.YELLOW));
        final int[] i = {0};
        this.sortByValue(items).forEach(entry -> {
            itemHover.append(ComponentText.toComponent(new TranslatableComponent(entry.getKey().getDescriptionId())).color(NamedTextColor.GRAY), ComponentText.of(": ").color(NamedTextColor.GRAY))
                    .append(ComponentText.of("<color:" + this.getHex(entry.getValue(), 500) + ">" + entry.getValue() + ((i[0] % 3 == 2) ? "\n" : " ")));
            i[0]++;
        });
        TextComponent.Builder builder = Component.text().content("There are currently ").color(NamedTextColor.YELLOW)
                .append(Component.text(itemCount).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(itemHover.build())),
                        Component.text(" items loaded.").color(NamedTextColor.YELLOW));

        this.getCommandSource(ctx).sendMessage(builder.build());
        return itemCount;
    }

    private int executePlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayers(ctx, "player").iterator().next();
        HashMap<EntityType<?>, Integer> entitiesByType = new HashMap<>();
        int entities = 0;
        for (Entity entity : player.getLevel().getAllEntities()) {
            if (entity.chunkPosition().getChessboardDistance(player.chunkPosition()) <= ServerSettings.getViewDistance()) {
                entitiesByType.put(entity.getType(), entitiesByType.getOrDefault(entity.getType(), 0) + 1);
                entities++;
            }
        }
        TextComponent.Builder entityHover = Component.text().content("Entities by Type:\n").color(NamedTextColor.YELLOW);
        this.sortByValue(entitiesByType).forEach(entry -> {
            entityHover.append(Component.text(entry.getKey().getDescription().getString()).color(NamedTextColor.GRAY),
                    Component.text("(").color(NamedTextColor.DARK_GRAY),
                    Component.text(entry.getKey().getCategory().getName()).color(NamedTextColor.AQUA),
                    Component.text(")").color(NamedTextColor.DARK_GRAY),
                    Component.text(": ").color(NamedTextColor.GRAY),
                    Component.text(entry.getValue() + "\n").color(NamedTextColor.LIGHT_PURPLE));
        });

        TextComponent.Builder builder = Component.text().content(player.getScoreboardName()).color(NamedTextColor.GOLD)
                .append(Component.text(" currently loads ").color(NamedTextColor.YELLOW),
                        Component.text(entities).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(entityHover.build())),
                        Component.text(" entities.").color(NamedTextColor.YELLOW));
        this.getCommandSource(ctx).sendMessage(builder.build());
        return entities;
    }

    private int executeType(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceLocation filter = EntitySummonArgument.getSummonableEntity(ctx, "type");
        HashMap<ServerPlayer, Integer> nearbyEntities = new HashMap<>();
        int entities = 0;
        int players = 0;
        EntityType<?> entityType = Registry.ENTITY_TYPE.get(filter);
        for (ServerLevel world : KiloEssentials.getMinecraftServer().getAllLevels()) {
            players += world.players().size();
            for (Entity entity : world.getAllEntities()) {
                if (!entity.getType().equals(entityType)) continue;
                for (ServerPlayer player : world.players()) {
                    int i = nearbyEntities.getOrDefault(player, 0);
                    if (entity.chunkPosition().getChessboardDistance(player.chunkPosition()) <= ServerSettings.getViewDistance() && entity.getCommandSenderWorld().equals(player.getCommandSenderWorld())) {
                        i++;
                    }
                    nearbyEntities.put(player, i);
                }
                entities++;
            }
        }
        TextComponent.Builder playersHover = Component.text()
                .append(ComponentText.toComponent(entityType.getDescription()).color(NamedTextColor.YELLOW), Component.text("s / Player :\n").color(NamedTextColor.YELLOW));
        this.sortByValue(nearbyEntities).forEach(entry -> {
            playersHover.append(ComponentText.of(entry.getKey().getScoreboardName() + ": ").color(NamedTextColor.GRAY))
                    .append(Component.text(entry.getValue() + "\n").color(NamedTextColor.LIGHT_PURPLE));
        });
        TextComponent.Builder builder = Component.text().content("There are currently ").color(NamedTextColor.YELLOW)
                .append(Component.text(entities + " ").color(NamedTextColor.GOLD),
                        ComponentText.toComponent(entityType.getDescription()).color(NamedTextColor.GOLD),
                        Component.text(" loaded, by ").color(NamedTextColor.YELLOW),
                        Component.text(players).color(NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(playersHover.build())),
                        Component.text(" players.").color(NamedTextColor.YELLOW));

        this.getCommandSource(ctx).sendMessage(builder.build());
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
