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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class QueryExecutor {

    void save(String tableName, Map<String, Object> values) {
        int valuesNumber = values.size();

        String keyString = getKeyString(values);
        String valuesPlaceHolder = getPlaceHolder(valuesNumber);
        String query = String.format("INSERT INTO %s (%s) VALUES (%s);", tableName, keyString,
            valuesPlaceHolder);

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
            throw new OrmException("Failed to connect to the database", e);
        }

    }

    Map<String, Object> getById(String tableName, Object id, int fieldsNumber) {
        String query = String.format("SELECT * FROM %s where id = %s", tableName, id);
        var result = getResultByQuery(query, fieldsNumber);
        if (result.size() > 1) {
            throw new OrmException("Expected 1 row with specified id, found " + result.size());
        } else {
            return result.get(0);
        }
    }


    List<Map<String, Object>> getAll(String tableName, int fieldsNumber) {
        String query = String.format("SELECT * FROM %s", tableName);
        return getResultByQuery(query, fieldsNumber);
    }

    void deleteById(String tableName, Object id) {
        String query = String.format("DELETE FROM %s WHERE id = %s", tableName, id);
        try (Connection connection = DriverManager.getConnection(dbURL, dbUser, dbPass)) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new OrmException("Error while executing query", e);
            }
        } catch (SQLException e) {
            throw new OrmException("Failed to connecto to the database", e);
        }
    }

    private List<Map<String, Object>> getResultByQuery(String query, int fieldsNumber) {
        List<Map<String, Object>> res = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(dbURL, dbUser, dbPass)) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.isBeforeFirst()) {        // if 0 rows in result
                    return Collections.emptyList();
                }
                ResultSetMetaData metaData = resultSet.getMetaData();
                while(resultSet.next()) {
                    Map<String, Object> temp = new HashMap<>();
                    for (int i = 1; i <= fieldsNumber; i++) {             // from 1 because ResultSet starts from 1
                        String columnName = metaData.getColumnName(i);

                        int columnType = metaData.getColumnType(i);
                        switch (columnType) {
                            case Types.TINYINT, Types.SMALLINT, Types.INTEGER -> {
                                Integer value = resultSet.getInt(i);
                                temp.put(columnName, value);
                            }
                            case Types.VARCHAR, Types.NVARCHAR -> {
                                String value = resultSet.getString(i);
                                temp.put(columnName, value);
                            }
                            case Types.BIGINT -> {
                                Long value = resultSet.getLong(i);
                                temp.put(columnName, value);
                            }
                            case Types.BOOLEAN -> {
                                Boolean value = resultSet.getBoolean(i);
                                temp.put(columnName, value);
                            }
                            case Types.BIT -> {       // for MySql, MySql has tinyint (bit) for boolean
                                Boolean value =
                                    resultSet.getByte(i) == 0 ? Boolean.FALSE : Boolean.TRUE;
                                temp.put(columnName, value);
                            }
                            default -> throw new OrmException("Type of the value from db '" + columnType + "' is not supported yet");
                        }
                    }
                    res.add(temp);
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
