package org.kilocraft.essentials.api.command.commandImpl;

import com.sun.corba.se.impl.activation.CommandHandler;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.command.CommandRegistry;
import org.kilocraft.essentials.api.command.KiloCommand;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistryImpl implements CommandRegistry {
    private Map<String, List<CommandRegistry>> handlers = new HashMap<>();

    @Override
    public void register(CommandRegistry commndClass) {
        for (Type type : commndClass.getClass().getGenericInterfaces()) {
            if  (!(type instanceof ParameterizedType))
                continue;
            if (!type.getTypeName().contains(CommandHandler.class.getTypeName()))
                continue;

            Type[] genericTypes = ((ParameterizedType) type).getActualTypeArguments();
            for (Type genericType : genericTypes) {
                String commandKey = genericType.getTypeName().replace("class ", "");

                if (!handlers.containsKey(commandKey)) {
                    handlers.put(commandKey, new ArrayList<>());
                }

                if (Mod.isDebugEnabled()) {
                    Mod.getLogger().debug("KiloAPI: Registering command class " + commndClass.getClass().getName());
                }

                handlers.get(commandKey).add(commndClass);
            }
            Mod.getLogger().info("KiloAPI: Successfully registerd %s commands!", handlers.size());
        }
    }

    @Override
    public <C extends KiloCommand> C register(C c) {
        String commandName = c.getClass().getInterfaces()[0].getTypeName();
        List<CommandRegistry> handlerList = handlers.get(commandName);

        if (handlerList == null) return c;

        handlerList.forEach(h -> h.register(c));
        return c;
    }

    @Override
    public Map getHandlers() {
        return handlers;
    }
}
