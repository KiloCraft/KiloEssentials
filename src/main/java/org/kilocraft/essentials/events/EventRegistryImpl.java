package org.kilocraft.essentials.events;

import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.EventRegistry;

import org.apache.logging.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventRegistryImpl implements EventRegistry {
    private final Map<String, List<EventHandler<?>>> handlers = new HashMap<>();
    private final Logger logger = LogManager.getLogger("KiloEssentials|EventRegistry");

    @Override
    public <E extends EventHandler<?>> void register(@NotNull final E handlerClass) {
        for (Type type : handlerClass.getClass().getGenericInterfaces()) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }

            if (!type.getTypeName().contains(EventHandler.class.getTypeName())) {
                continue;
            }

            Type[] genericTypes = ((ParameterizedType) type).getActualTypeArguments();
            for (Type genericType : genericTypes) {
                String eventKey = genericType.getTypeName().replace("class ", "");

                if (!handlers.containsKey(eventKey)) {
                    handlers.put(eventKey, new ArrayList<>());
                }

                handlers.get(eventKey).add(handlerClass);
                if (SharedConstants.isDevelopment) {
                    logger.info(
                            "Registering callback \"{}\" for event {}",
                            handlerClass.getClass().getSimpleName(),
                            genericType.getTypeName().substring(genericType.getTypeName().lastIndexOf('.'))
                    );
                }
            }
        }
    }

    public <E extends Event> E trigger(@NotNull final E e) {
        String eventName = e.getClass().getInterfaces()[0].getTypeName();
        List<EventHandler<?>> handlerList = handlers.get(eventName);

        if (handlerList == null) {
            return e;
        }

        for (EventHandler handler : handlerList) {
            handler.handle(e);
        }

        return e;
    }

    @NotNull
    @Override
    public Map<String, List<EventHandler<?>>> getHandlers() {
        return handlers;
    }

}