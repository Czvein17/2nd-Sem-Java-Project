package com.javaproject;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectDB {
    Connection connection;
    String dbname = "javafx";
    String username = "root";
    String password = "#17Czvein";

    public Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/" + dbname, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}