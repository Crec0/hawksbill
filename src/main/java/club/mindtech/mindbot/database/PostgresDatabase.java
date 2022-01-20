package club.mindtech.mindbot.database;

import club.mindtech.mindbot.config.Config;

import java.sql.*;
import java.util.Properties;

public class PostgresDatabase {

    public static void connectToDatabase() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", Config.DBUSER);
        props.setProperty("password", Config.DBPASS);
        props.setProperty("ssl", Config.DBSSL);

        Connection con = DriverManager.getConnection(Config.DBURL, props);
        Statement statement = con.createStatement();

        createTables(statement);
    }

    private static void createTables(Statement statement) throws SQLException {
        for (Table table : Table.values()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + table.getName() + " " + table.getDataTypes());
        }
    }
}
