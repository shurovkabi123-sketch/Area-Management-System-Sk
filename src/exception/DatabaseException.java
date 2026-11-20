package exception;

/**
 * Checked exception for any database problem.
 * The message is always written for the END USER (not a raw SQL error),
 * so the GUI can show it directly in a dialog.
 */
public class DatabaseException extends Exception {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
