package server.networking.database;

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
        assert this.url != null;
        assert this.username != null;
        assert this.password != null;

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

    public void executeQuery(String query) throws SQLException {
        this.connection.createStatement().execute(query);
    }

    public ResultSet executeQueryWithResult(String query) throws SQLException {
        return this.connection.createStatement().executeQuery(query);
    }
}
