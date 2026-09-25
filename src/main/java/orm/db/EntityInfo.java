package orm.db;

import java.lang.reflect.Field;
import java.util.Map;

public class EntityInfo {
    private String tableName;

    private Map<String, Field> fields;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public void setFields(Map<String, Field> fields) {
        this.fields = fields;
    }
}
