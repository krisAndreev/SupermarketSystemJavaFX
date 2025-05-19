package org.supermarket.supermarketsystemjavafx.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection
{
    private static final String DB_URL = "jdbc:sqlite:supermarket.db";

    public static Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(DB_URL);
    }

    public static void closeConnection(Connection conn)
    {
        try
        {
            if (conn != null)
            {
                conn.close();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}