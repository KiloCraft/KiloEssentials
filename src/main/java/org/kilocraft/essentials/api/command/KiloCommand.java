package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.util.SomeGlobals;

import java.util.List;

public abstract class KiloCommand implements IKiloCommand {
    private CommandDispatcher<ServerCommandSource> dispatcher = SomeGlobals.commandDispatcher;
    private String name;
    private String permissionNode;
    private int permissionLevel;
    private boolean registered;
    private List<String> commandAliases;

    public KiloCommand(String name, String permissionNode, int permissionLevel) {
        this.name = name;
        this.permissionNode = name;
        this.permissionLevel = permissionLevel;
        this.registered = true;
    }


    public String getName() {
        return this.name;
    }

    public String getPermissionNode() {
        return this.permissionNode;
    }

    public int getPermissionLevel() {
        return this.permissionLevel;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public CommandDispatcher<ServerCommandSource> getDispatcher() {
        return dispatcher;
    }


}
