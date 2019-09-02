package org.kilocraft.essentials.mysql;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.kilocraft.essentials.KiloDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class home {

    public static void setHome(ServerPlayerEntity player, String name, World world) throws SQLException {
        double x = player.getPos().getX();
        double y = player.getPos().getX();
        double z = player.getPos().getX();
        Connection connection = KiloDatabase.getConnection();
        String query = "insert into user_homes (owner_uuid, owner_name, home_name, x, y, z, world)"
                + " values (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stat = connection.prepareStatement(query);
        stat.setString(1, player.getUuidAsString());
        stat.setString(2, player.getEntityName());
        stat.setString(3, name);
        stat.setDouble(4, x);
        stat.setDouble(5, y);
        stat.setDouble(6, z);
        stat.setString(7, String.valueOf(world));
        stat.execute();
    }

}
