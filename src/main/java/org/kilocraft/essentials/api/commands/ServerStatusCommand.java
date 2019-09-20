package org.kilocraft.essentials.api.commands;

import org.kilocraft.essentials.api.command.KiloCommand;

import java.util.ArrayList;

public class ServerStatusCommand extends KiloCommand{

    public ServerStatusCommand(String name, String permissionNode, int permissionLevel) {
        super("test", "kapi.command.test", 4);

    }


    @Override
    public void getCommand() {

    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public ArrayList<String> getAliases() {
        return new ArrayList<String>(){{
            add("testalias");
        }};
    }

}