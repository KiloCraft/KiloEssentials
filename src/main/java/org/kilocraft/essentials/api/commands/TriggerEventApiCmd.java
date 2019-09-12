package org.kilocraft.essentials.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;

import java.util.List;
import java.util.Map;

public class TriggerEventApiCmd {
    private static Map handlers = KiloServer.getServer().getEventRegistry().getHandlers();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("kapi").then(
                CommandManager.literal("triggerEvent")
                    .then(CommandManager.argument("group", StringArgumentType.string()).then(
                            CommandManager.argument("event", StringArgumentType.string()).then(
                                CommandManager.argument("player", EntityArgumentType.player())
                            )
                    ))
        );

        buildSuggestion(argumentBuilder);
        dispatcher.register(argumentBuilder);
    }

    private static int execute(ServerCommandSource source, String event, ServerPlayerEntity player, String message) {
        LiteralText literalText = new LiteralText(String.format("Triggering the event (playerEntity: %s, message: %s)", event, player.getName(), message));
        source.sendFeedback(literalText.formatted(Formatting.GRAY, Formatting.ITALIC), false);

        Class<?> eventClass;

        try {
            eventClass = Class.forName(event);

            String eventName = eventClass.getClass().getInterfaces()[0].getTypeName();
            List<EventHandler> handlerList = (List<EventHandler>) KiloServer.getServer().getEventRegistry().getHandlers().get(eventName);
            handlerList.forEach(handler -> handler.handle(event));


        } catch (ClassNotFoundException e) {
            source.sendFeedback(new LiteralText("Can not find the event \"" + event + "\"!").formatted(Formatting.RED), false);
        }



        return 1;
    }

    private static void buildSuggestion(LiteralArgumentBuilder argumentBuilder) {
        handlers.forEach((name, event) -> {

        });

    }
}
