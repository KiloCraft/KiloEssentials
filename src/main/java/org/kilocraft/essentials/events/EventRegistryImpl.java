package org.kilocraft.essentials.events;

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

    private Map<String, List<EventHandler>> handlers = new HashMap<>();
    private Logger logger = ModConstants.getLogger();

    public void register(EventHandler eventClass) {
        for (Type type : eventClass.getClass().getGenericInterfaces()) {
            if (!(type instanceof ParameterizedType))
                continue;

            if (!type.getTypeName().contains(EventHandler.class.getTypeName()))
                continue;

            Type[] genericTypes = ((ParameterizedType) type).getActualTypeArguments();
            for (Type genericType : genericTypes) {
                String eventKey = genericType.getTypeName().replace("class ", "");

                if (!handlers.containsKey(eventKey)) {
                    handlers.put(eventKey, new ArrayList<>());
                }

                logger.debug("KiloAPI: Registering event class " + eventClass.getClass().getName() + " for event " + eventKey);
                handlers.get(eventKey).add(eventClass);
            }

            logger.debug("KiloAPI: Successfully registered the events!");
            logger.debug(handlers.toString());
        }
    }

    public <E extends Event> E trigger(E e) {
        String eventName = e.getClass().getInterfaces()[0].getTypeName();
        List<EventHandler> handlerList = handlers.get(eventName);

        if (handlerList == null)
            return e;

        handlerList.forEach(handler -> handler.handle(e));

        return e;
    }

    @Override
    public Map getHandlers() {
        return handlers;
    }

}