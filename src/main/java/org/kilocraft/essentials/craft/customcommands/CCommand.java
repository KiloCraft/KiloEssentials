package org.kilocraft.essentials.craft.customcommands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;

import java.util.List;

public class CCommand {
    private String name;
    private String permission_node;
    private int op_level;
    private String executes;
    private List<String> aliases;

    public CCommand(String name, String permission_node, int op_level, String executes, List<String> aliases) {
        this.name = name;
        this.permission_node = permission_node;
        this.op_level = op_level;
        this.executes = executes;
        this.aliases = aliases;
    }

    private static CommandDispatcher<ServerCommandSource> dispatcher = KiloServer.getServer().getCommandRegistry().getDispatcher();

    private static void getCommands() {

    }

}
