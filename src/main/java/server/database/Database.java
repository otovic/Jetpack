package server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
    public String url;
    public String username;
    public String password;

    private Connection connection;

    public Database(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void connect() throws SQLException {
        if (this.connection == null) return;
        if (this.url == null) return;
        if (this.username == null) return;

        this.connection = DriverManager.getConnection(this.url, this.username, this.password);

        if (this.connection == null) {
            throw new SQLException("Failed to connect to the database!");
        }
    }

    public void disconnect() throws SQLException {
        this.connection.close();
    }

    public void startDatabaseSession() throws SQLException {
        this.connect();
    }

    public String executeQuery(String query) throws SQLException {
        try {
            this.connection.createStatement().execute(query);
            return "Success";
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                return "Duplicate entry";
            }
            return "Failed";
        }
    }

    public ResultSet executeQueryWithResult(String query) throws SQLException {
        return this.connection.createStatement().executeQuery(query);
    }

    @Override
    public String toString() {
        return "Database [connection=" + connection + ", password=" + password + ", url=" + url + ", username="
                + username + "]";
    }
}
