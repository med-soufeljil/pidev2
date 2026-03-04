package org.soa.tp1.pi_dev_s2.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    // ✅ URL JDBC MySQL, avec timezone
    private static final String URL =
            "jdbc:mysql://localhost:3306/PI_DEV_3a11_2?useSSL=false&serverTimezone=UTC";

    // 🔹 Change USER et PASSWORD selon ton MySQL
    private static final String USER = "root";       // ou "devuser"
    private static final String PASSWORD = "";   // mot de passe correct

    public static Connection getConnection() {
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL chargé");

            // Retourner la connexion
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion à la base réussie");
            return conn;

        } catch (ClassNotFoundException e) {
            System.out.println("Driver MySQL introuvable");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Erreur de connexion à la base de données");
            e.printStackTrace();
        }

        return null; // si échec
    }
}
