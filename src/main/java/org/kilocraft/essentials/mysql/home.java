package org.kilocraft.essentials.mysql;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.kilocraft.essentials.KiloDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class home {

    Connection connection = KiloDatabase.getConnection();

    public void setHome(ServerPlayerEntity player, String name, int x, int y, int z, World world) throws SQLException {

        String query = "insert into user_homes (owner_uuid, owner_name, home_name, x, y, z, world)"
                + " values (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stat = connection.prepareStatement(query);
        stat.setString(1, player.getUuidAsString());
        stat.setString(2, player.getEntityName());
        stat.setString(3, name);
        stat.setInt(4, x);
        stat.setInt(5, y);
        stat.setInt(6, z);
        stat.setString(7, String.valueOf(world));
        stat.execute();
    }

}
