package org.soa.tp1.pi_dev_s2.mouhamd.utils;

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

    private void ensureSchema() {
        String create = "CREATE TABLE IF NOT EXISTS formation_feedback ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "id_formation INT NOT NULL,"
                + "author VARCHAR(120) NOT NULL,"
                + "rating INT NOT NULL,"
                + "comment_text TEXT NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Statement st = connection.createStatement()) {
            st.executeUpdate(create);
        } catch (SQLException ignored) {
        }

        try (Statement st = connection.createStatement()) {
            st.executeUpdate("ALTER TABLE formation_feedback ADD COLUMN comment_text TEXT NULL");
            st.executeUpdate("UPDATE formation_feedback SET comment_text = comment WHERE comment_text IS NULL");
        } catch (SQLException ignored) {
            // compatibility with existing schema/user rights
        }
    }
}
