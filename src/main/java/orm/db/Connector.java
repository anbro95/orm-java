package orm.db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Connector {

    private static final String PROPERTIES_PATH = "src/main/resources/application.properties";
    protected static String dbURL;
    protected static String dbUser;
    protected static String dbPass;
    protected static boolean showSql;
    protected static String ddlAuto; // create / create-drop / update
    private static Properties properties;

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
        dbURL = getProperty("db.url");
        dbUser = getProperty("db.user");
        dbPass = getProperty("db.password");
        showSql = Boolean.parseBoolean(getProperty("orm.showSql"));
        ddlAuto = getProperty("orm.ddl.auto");
    }

    private static String getProperty(String name) {
        if (properties == null) {
            properties = new Properties();
            try (FileInputStream fis = new FileInputStream(PROPERTIES_PATH)) {
                properties.load(fis);
            } catch (FileNotFoundException e) {
                throw new OrmException("Could not find properties file", e);
            } catch (IOException e) {
                throw new OrmException("Could not load properties", e);
            }
        }

        return properties.getProperty(name);
    }
}
