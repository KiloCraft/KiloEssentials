package org.kilocraft.essentials.config_old.provided.localVariables;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.config_old.variablehelper.LocalConfigVariable;
import org.kilocraft.essentials.util.TPSTracker;

import java.util.HashMap;

public class ServerConfigVariables implements LocalConfigVariable {
    private static Server server = KiloServer.getServer();

    public ServerConfigVariables() {
    }

    @Override
    public String getPrefix() {
        return "SERVER";
    }

    @Override
    public HashMap<String, String> variables() {
        return new HashMap<String, String>(){{
            put("NAME", server.getName());
            put("TPS", TPSTracker.tps1.getShortAverage());
            put("FORMATTED_TPS", "&" + TextFormat.getFormattedTPS(TPSTracker.tps1.getAverage()) + TPSTracker.tps1.getShortAverage() + "&r");
            put("PLAYER_COUNT", String.valueOf(server.getPlayerManager().getCurrentPlayerCount()));
        }};
    }
}
