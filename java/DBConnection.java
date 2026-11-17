import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/test";
    private static final String USER = "root";
    private static final String PASSWORD = "ProjectDB@";

    private static final String JAR_NAME = "mysql-connector-j-26.10.0.jar";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private static Driver driver; // loaded once and reused

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASSWORD);

        // Use the jar from the lib folder if we can find it
        Driver d = loadDriver();
        if (d != null) {
            Connection con = d.connect(URL, props);
            if (con != null) {
                return con;
            }
        }

        // Fallback: the driver is already on the classpath
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static synchronized Driver loadDriver() {
        if (driver != null) {
            return driver;
        }

        File jar = findJar();
        if (jar == null) {
            return null;
        }

        try {
            URLClassLoader loader = new URLClassLoader(
                new URL[] { jar.toURI().toURL() },
                DBConnection.class.getClassLoader());

            Class<?> cls = Class.forName(DRIVER_CLASS, true, loader);
            driver = (Driver) cls.getDeclaredConstructor().newInstance();
            return driver;
        } catch (Exception e) {
            return null;
        }
    }

    private static File findJar() {
        String[] candidates = {
            "lib/" + JAR_NAME,
            "java/lib/" + JAR_NAME,
            "../java/lib/" + JAR_NAME
        };

        for (String path : candidates) {
            File f = new File(path);
            if (f.exists()) {
                return f.getAbsoluteFile();
            }
        }
        return null;
    }
}