package orm.db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

// TODO to think if it's needed to be static or not
public class Connector {

    private static final String PROPERTIES_PATH = "src/main/resources/application.properties";
    protected static String dbURL;
    protected static String dbUser;
    protected static String dbPass;
    protected static boolean showSql;

    static {
        init();
    }

//  public static Connection getConnection() {
//    try (Connection con = DriverManager.getConnection(dbURL, dbUser, dbPass)) {
//      return con;
//    } catch (SQLException e) {
//      throw new OrmException("Could not connect to the database", e);
//    }
//  }

    private static void init() {
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(PROPERTIES_PATH)) {
            properties.load(fis);

            dbURL = properties.getProperty("db.url");
            dbUser = properties.getProperty("db.user");
            dbPass = properties.getProperty("db.password");

            showSql = Boolean.parseBoolean(properties.getProperty("orm.showSql"));


        } catch (FileNotFoundException e) {
            throw new OrmException("Could not find properties file", e);
        } catch (IOException e) {
            throw new OrmException("Could not load properties", e);
        }
    }
}
