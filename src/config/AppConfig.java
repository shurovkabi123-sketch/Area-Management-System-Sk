package config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Reads application settings from an EXTERNAL file (db.properties),
 * so no database password is ever written inside the source code.
 *
 * Lookup order for each setting:
 *   1. Environment variable (ACMS_DB_URL, ACMS_DB_USER, ACMS_DB_PASSWORD, ACMS_UPLOAD_DIR)
 *   2. db.properties file in the working directory
 *      (or the path given by -Dacms.config=/path/to/file)
 */
public final class AppConfig {

    private static final Properties PROPS = new Properties();
    private static final Path CONFIG_FILE =
            Paths.get(System.getProperty("acms.config", "db.properties")).toAbsolutePath();
    private static String loadProblem = null;

    static {
        if (Files.exists(CONFIG_FILE)) {
            try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
                PROPS.load(in);
            } catch (IOException e) {
                loadProblem = "Could not read configuration file:\n" + CONFIG_FILE
                        + "\n" + e.getMessage();
            }
        }
    }

    private AppConfig() { }   // utility class: no objects

    private static String read(String key, String envName) {
        String env = System.getenv(envName);
        if (env != null && !env.isEmpty()) {
            return env;
        }
        return PROPS.getProperty(key, "");
    }

    public static String getDbUrl()      { return read("db.url", "ACMS_DB_URL").trim(); }
    public static String getDbUser()     { return read("db.user", "ACMS_DB_USER").trim(); }
    public static String getDbPassword() { return read("db.password", "ACMS_DB_PASSWORD"); }

    public static String smtpHost() { return read("smtp.host", "ACMS_SMTP_HOST").trim(); }
    public static String smtpPort() { String v=read("smtp.port", "ACMS_SMTP_PORT").trim(); return v.isEmpty()?"587":v; }
    public static String smtpUser() { return read("smtp.user", "ACMS_SMTP_USER").trim(); }
    public static String smtpPassword() { return read("smtp.password", "ACMS_SMTP_PASSWORD"); }
    public static String smtpFrom() { return read("smtp.from", "ACMS_SMTP_FROM").trim(); }

    public static Path getUploadDir() {
        String dir = read("upload.dir", "ACMS_UPLOAD_DIR").trim();
        return Paths.get(dir.isEmpty() ? "uploads" : dir).toAbsolutePath();
    }

    public static boolean isConfigured() {
        return loadProblem == null && !getDbUrl().isEmpty() && !getDbUser().isEmpty();
    }

    /** Human-readable explanation of what is wrong with the configuration. */
    public static String describeProblem() {
        if (loadProblem != null) {
            return loadProblem;
        }
        return "Database settings not found.\n\n"
                + "Copy db.properties.example to db.properties in the project folder\n"
                + "and enter your MySQL username and password.\n\n"
                + "Expected file location:\n" + CONFIG_FILE;
    }
}
