package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = EnvConfig.get("DB_URL");
    private static final String USER = EnvConfig.get("DB_USER");
    private static final String PASSWORD = EnvConfig.get("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
