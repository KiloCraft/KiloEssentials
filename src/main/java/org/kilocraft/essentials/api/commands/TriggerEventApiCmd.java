package org.kilocraft.essentials.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;

import java.util.List;

public class TriggerEventApiCmd {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("kiloserver").then(
                CommandManager.literal("triggerEvent")
                        .requires(source -> source.hasPermissionLevel(4))
        );

        argumentBuilder.then(CommandManager.argument("event", StringArgumentType.string())
                .then(CommandManager.argument("FakeServerPlayerEntity", StringArgumentType.string()))
                .then(CommandManager.argument("setMessage", StringArgumentType.greedyString())
                    .executes(context -> execute(context.getSource(),
                            StringArgumentType.getString(context, "event"),
                            StringArgumentType.getString(context, "FakeServerPlayerEntity"),
                            StringArgumentType.getString(context, "setMessage")))
                )
        );


        dispatcher.register(argumentBuilder);
    }

    private static int execute(ServerCommandSource source, String event, String player, String message) {
        LiteralText literalText = new LiteralText(String.format("Triggering \"%s\" (playerEntity: %s, message: %s)", event, player, message));
        source.sendFeedback(literalText.setStyle(new Style().setColor(Formatting.GRAY).setItalic(true)), false);

        Class<?> eventClass = null;

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

    private static void buildSuggestion() {

    }
}
