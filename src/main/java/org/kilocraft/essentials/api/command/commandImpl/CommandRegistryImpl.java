package org.kilocraft.essentials.api.command.commandImpl;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.command.CommandRegistry;
import org.kilocraft.essentials.api.command.KiloCommand;
import org.kilocraft.essentials.api.util.SomeGlobals;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistryImpl implements CommandRegistry {
    private Map<String, List<KiloCommand>> handlers = new HashMap<>();

    @Override
    public CommandDispatcher<ServerCommandSource> getDispatcher() {
        return SomeGlobals.commandDispatcher;
    }

    @Override
    public void addCommand(@NotNull KiloCommand commndClass) {

        for (Type type : commndClass.getClass().getGenericInterfaces()) {
            if  (!(type instanceof ParameterizedType))
                continue;
            if (!type.getTypeName().contains(CommandRegistry.class.getTypeName()))
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
    public void removeCommand(@NotNull KiloCommand commandClass) {
        for (Type type : commandClass.getClass().getGenericInterfaces()) {
            if (!(type instanceof ParameterizedType))
                continue;
            if (!type.getTypeName().contains(CommandRegistry.class.getTypeName()))
                continue;

            Type[] genericTypes = ((ParameterizedType) type).getActualTypeArguments();
            for (Type genericType : genericTypes) {
                String commandKey = genericType.getTypeName().replace("class ", "");

                if (!handlers.containsKey(commandKey)) {
                    handlers.put(commandKey, new ArrayList<>());
                }

                if (Mod.isDebugEnabled()) {
                    Mod.getLogger().debug("KiloAPI: Registering command class " + commandClass.getClass().getName());
                }

                handlers.get(commandKey).remove(commandClass);
            }
        }
    }

    @Override
    public <C extends KiloCommand> C register(C c) {
        String commandName = c.getClass().getInterfaces()[0].getTypeName();
        List<KiloCommand> handlerList = handlers.get(commandName);

        if (handlerList == null) return c;

        //handlerList.forEach(h -> h.commandBuilder());
        return c;
    }

    @Override
    public <C extends KiloCommand> C unregister(C c) {
        return c;
    }

    @Override
    public void registerAll() {

    }

    @Override
    public void unregisterAll() {

    }

    @Override
    public Map getHandlers() {
        return handlers;
    }
}
