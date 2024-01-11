package server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database klasa pomocu oje se ostvaruje konekcija na bazu.
 */
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

    /**
     * Ostvaruje konekciju na bazu.
     * 
     * @throws SQLException ako konekcija na bazu ne uspe
     */
    public void connect() throws SQLException {
        if (this.password == null) return;
        if (this.url == null) return;
        if (this.username == null) return;

        this.connection = DriverManager.getConnection(this.url, this.username, this.password);

        if (this.connection == null) {
            throw new SQLException("Failed to connect to the database!");
        }
    }

    /**
     * gasi konekciju ka bazi.
     * @throws SQLException ako se desi greska prilikom gasenja konekcije na bazu.
     */
    public void disconnect() throws SQLException {
        this.connection.close();
    }

    /**
     * Izvrsava zadatai query nad datom bazom podataka.
     * 
     * @param query query koji setreba izvrsiti
     * @return vraca poruku o uspesnosti izvrsavanja query-a
     * @throws SQLException ako se desi greska prilikom izvrsavanja query-a
     */
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

    /**
        * Ako query treba da vraca podatke iz baze koristi se ova metoda.
        *
        * @param query query koji treba da se izvrsi
        * @return vraca ResultSet objekat koji sadrzi podatke iz baze
        * @throws SQLException ako se desi greska prilikom izvrsavanja query-a
        */
    public ResultSet executeQueryWithResult(String query) throws SQLException {
        return this.connection.createStatement().executeQuery(query);
    }

    @Override
    public String toString() {
        return "Database [connection=" + connection + ", password=" + password + ", url=" + url + ", username="
                + username + "]";
    }
}
