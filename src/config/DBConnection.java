package config;

import exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralised place that creates database connections.
 *
 * Every DAO calls DBConnection.getConnection() inside a try-with-resources
 * block, so the connection is always closed automatically:
 *
 *     try (Connection con = DBConnection.getConnection()) { ... }
 */
public final class DBConnection {

    static {
        DriverManager.setLoginTimeout(5);   // fail fast (5 s) if MySQL is not reachable
    }

    private DBConnection() { }

    /** Opens a NEW connection. The caller is responsible for closing it. */
    public static Connection getConnection() throws DatabaseException {
        if (!AppConfig.isConfigured()) {
            throw new DatabaseException(AppConfig.describeProblem());
        }
        try {
            return DriverManager.getConnection(
                    AppConfig.getDbUrl(),
                    AppConfig.getDbUser(),
                    AppConfig.getDbPassword());
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    /** Converts technical SQL errors into friendly messages. */
    public static DatabaseException translate(SQLException e) {
        int code = e.getErrorCode();
        String state = e.getSQLState() == null ? "" : e.getSQLState();

        if (code == 1045) {
            return new DatabaseException(
                    "Access denied: the MySQL username or password in db.properties is wrong.", e);
        }
        if (code == 1049) {
            return new DatabaseException(
                    "Database 'area_complaint_db' does not exist.\n"
                    + "Run sql/area_complaint_db.sql in MySQL first.", e);
        }
        if (state.startsWith("08")) {
            return new DatabaseException(
                    "Cannot reach the MySQL server.\n"
                    + "Make sure MySQL is running and the host/port in db.properties are correct.", e);
        }
        if (code == 1062) {
            return new DatabaseException("This record already exists (duplicate value).", e);
        }
        return new DatabaseException("Database error: " + e.getMessage(), e);
    }
}
