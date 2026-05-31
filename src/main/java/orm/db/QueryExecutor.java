package orm.db;

import static orm.db.Connector.dbPass;
import static orm.db.Connector.dbURL;
import static orm.db.Connector.dbUser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class QueryExecutor {

    void save(String tableName, Map<String, Object> values) {
        int valuesNumber = values.size();

        String keyString = getKeyString(values);
        String valuesPlaceHolder = getPlaceHolder(valuesNumber);
        String query = String.format("INSERT INTO %s (%s) VALUES (%s);", tableName, keyString,
            valuesPlaceHolder);

        System.out.println("Query: " + query);
        Iterator<Object> iterator = values.values().iterator();
        try (Connection con = DriverManager.getConnection(dbURL, dbUser, dbPass)) {
            try (PreparedStatement statement = con.prepareStatement(query)) {
                for (int i = 1; i <= valuesNumber; i++) {
                    statement.setObject(i, iterator.next());
                }

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new OrmException("Error while executing query", e);
            }
        } catch (SQLException e) {
            throw new OrmException("Could not connect to the database", e);
        }

    }

    Map<String, Object> getById(String tableName, Object id, int fieldsNumber) {
        Map<String, Object> res = new HashMap<>();
        String query = String.format("SELECT * FROM %s where id = %s", tableName, id);

        try (Connection connection = DriverManager.getConnection(dbURL, dbUser, dbPass)) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.isBeforeFirst()) {        // if 0 rows in result
                    return Collections.emptyMap();
                }
                ResultSetMetaData metaData = resultSet.getMetaData();
                resultSet.next();
                for (int i = 1; i <= fieldsNumber; i++) {             // from 1 because ResultSet starts from 1
                    String columnName = metaData.getColumnName(i);
                    System.out.println("ColumnName:" + columnName);

                    int columnType = metaData.getColumnType(i);
                    System.out.println("Index: " + i);
                    System.out.println("Type: " + columnType);
                    switch (columnType) {
                        case Types.TINYINT, Types.SMALLINT, Types.INTEGER -> {
                            Integer value = resultSet.getInt(i);
                            res.put(columnName, value);
                        }
                        case Types.VARCHAR, Types.NVARCHAR -> {
                            String value = resultSet.getString(i);
                            res.put(columnName, value);
                        }
                        case Types.BIGINT -> {
                            Long value = resultSet.getLong(i);
                            res.put(columnName, value);
                        }
                        case Types.BOOLEAN -> {
                            Boolean value = resultSet.getBoolean(i);
                            res.put(columnName, value);
                        }
                        case Types.BIT -> {       // for MySql, MySql has tinyint (bit) for boolean
                            Boolean value =
                                resultSet.getByte(i) == 0 ? Boolean.FALSE : Boolean.TRUE;
                            res.put(columnName, value);
                        }
                        default -> throw new OrmException(
                            "Type of the value from db is not supported yet");
                    }
                }
            } catch (SQLException e) {
                throw new OrmException("Error while executing query", e);
            }
        } catch (SQLException e) {
            throw new OrmException("Could not connect to the database", e);
        }

        return res;
    }


    private String getPlaceHolder(int number) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < number - 1; i++) {
            builder.append('?').append(',');
        }
        builder.append('?');
        return builder.toString();
    }

    private String getKeyString(Map<String, Object> values) {
        StringBuilder builder = new StringBuilder();
        for (String key : values.keySet()) {
            builder.append(key).append(',');
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

}
