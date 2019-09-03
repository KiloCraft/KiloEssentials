package org.kilocraft.essentials.craft.mysql;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.kilocraft.essentials.craft.KiloDatabase;
import org.kilocraft.essentials.craft.KiloEssentials;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class home {

    public static void setHome(ServerPlayerEntity player, String home_name, World entityWorld) {
        KiloEssentials.getLogger.info("setHome executed!");
        String uuid,name,world;
        double x,y,z;
        try {
            KiloDatabase.connect();
            Connection connection = KiloDatabase.getConnection();
            String query = "insert into user_homes (owner_uuid, owner_name, home_name, x, y, z, World)"
                    + " values (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stat = null;
            uuid = player.getUuidAsString();
            name = player.getEntityName();
            x = player.getPos().getX();
            y = player.getPos().getY();
            z = player.getPos().getZ();
            world = String.valueOf(entityWorld);
            KiloEssentials.getLogger.info(connection.isClosed());
            KiloEssentials.getLogger.info("uuid: " + uuid);
            KiloEssentials.getLogger.info("name: " + name);
            KiloEssentials.getLogger.info("home_name: " + home_name);
            KiloEssentials.getLogger.info("World: " + world);
            KiloEssentials.getLogger.info("x: " + x);
            KiloEssentials.getLogger.info("y: " + y);
            KiloEssentials.getLogger.info("z: " + z);


            stat = connection.prepareStatement(query);
            stat.setString(1, "7aefd3e5-83d0-491f-98fe-5402ae7204a6");
            stat.setString(2, "DrexHD");
            stat.setString(3, "spawn");
            stat.setDouble(4, -351.9116938952472);
            stat.setDouble(5, 127.0);
            stat.setDouble(6, 189.42757312864293);
            stat.setString(7, "World");
            stat.execute();
        } catch (Exception e) {
            KiloEssentials.getLogger.info("Error of MySQL query: " + e);
        }
    }

}