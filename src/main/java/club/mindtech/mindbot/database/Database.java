package club.mindtech.mindbot.database;

import club.mindtech.mindbot.MindBot;
import club.mindtech.mindbot.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Database {

    private static final Database INSTANCE = new Database();

    private Connection connection;

    private Database() {
        connectToDatabase();
    }

    public static Database getInstance() {
        return INSTANCE;
    }

    private void connectToDatabase() {
        Properties props = new Properties();
        props.setProperty("user", Config.DBUSER);
        props.setProperty("password", Config.DBPASS);
        props.setProperty("ssl", Config.DBSSL);

        try {
            this.connection = DriverManager.getConnection(Config.DBURL, props);
            this.createTables();
        } catch (SQLException e) {
            MindBot.LOGGER.error("Could not connect to database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        Statement statement = connection.createStatement();
        for (Table table : Table.values()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + table.getName() + " " + table.getDataTypes());
        }
        statement.close();
    }

    public void insert(Table table, String ...data) {
        try {
            Statement statement = connection.createStatement();
            statement.execute("INSERT INTO " + table.getName() + " VALUES (" + String.join(", ", data) + ")");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
