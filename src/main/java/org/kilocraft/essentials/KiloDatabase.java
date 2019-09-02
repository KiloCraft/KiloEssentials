package org.kilocraft.essentials;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.Connection;

public class KiloDatabase {

    public static Connection connection;

    public static void connect() throws Exception {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setServerName("88.198.50.85");
        dataSource.setPort(3306);
        dataSource.setDatabaseName("kiloessentials");
        dataSource.setUser("root");
        dataSource.setPassword("nywtU7mbSa6s8rpMU6OLdELASzZVrr");
        dataSource.setUseSSL(true);
        connection = dataSource.getConnection();
    }

    public static Connection getConnection() {
        return connection;
    }


}
