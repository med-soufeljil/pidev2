package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDatabase {
    private final String USER = "root";
    private final String PASSWORD = "";
    private final String URL = "jdbc:mysql://localhost:3306/pidev";
    public Connection connection;
    public static MyDatabase instance = new MyDatabase();

    private MyDatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            ensureSchema();
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void ensureSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS formation_feedback ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "id_formation INT NOT NULL,"
                    + "author VARCHAR(120) NOT NULL,"
                    + "rating INT NOT NULL,"
                    + "comment TEXT NOT NULL,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)" );
        } catch (SQLException ignored) {
            // keep app running even if user has restricted permissions
        }

    }
}
