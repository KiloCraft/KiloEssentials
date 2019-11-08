package org.kilocraft.essentials.craft.customcommands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.KiloCommands;

import java.util.ArrayList;

public class Command {
    protected String name;
    protected String permission_node;
    protected int op_level;
    protected ArrayList<String> executes, aliases;

    public Command(String name, String permission_node, int op_level, ArrayList<String> executes, ArrayList<String> aliases) {
        this.name = name;
        this.permission_node = permission_node;
        this.op_level = op_level;
        this.executes = executes;
        this.aliases = aliases;
    }


    private static CommandDispatcher<ServerCommandSource> dispatcher = KiloCommands.getDispatcher();

    public String getName() {
        return this.name;
    }

    public String getPermission_node() {
        return this.permission_node;
    }

    public int getOpLevel() {
        return this.op_level;
    }

    public ArrayList<String> getExecutes() {
        return this.executes;
    }

    public ArrayList<String> getAliases() {
        return this.aliases;
    }

    public static CommandDispatcher<ServerCommandSource> getDispatcher() {
        return dispatcher;
    }
}
