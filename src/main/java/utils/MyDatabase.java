package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private final String USER = "root";
    private final String PASSWORD = "";
    private final String URL = "jdbc:mysql://localhost:3306/pidev";
    public Connection connection;
    public static MyDatabase instance = new MyDatabase();
    private MyDatabase() {
        try{
        connection = DriverManager.getConnection(URL,USER,PASSWORD);
            System.out.println("Connected to database successfully");
    }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }
    public static MyDatabase getInstance() {
        if(instance == null){
            instance = new MyDatabase();
        }
        return instance;

    }
    public Connection getConnection() {
        return connection;
    }


}
