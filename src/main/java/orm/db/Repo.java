package orm.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import orm.annot.Column;
import orm.annot.Entity;
import orm.annot.Table;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class Repo<T, ID> {

    private final QueryExecutor executor;
    private final Class<T> clazz;
    private String tableName;
    private final Map<String, Field> fields;


    public Repo(Class<T> clazz) {
        this.executor = new QueryExecutor();
        this.clazz = clazz;
        this.fields = new HashMap<>();
        inspectClass(clazz);
    }

    public void save(T obj) {

        Field[] fields = clazz.getDeclaredFields();
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        for (Field field : fields) {
            Column colAnn = field.getAnnotation(Column.class);
            String name = colAnn == null ? field.getName() : colAnn.name();
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                fieldMap.put(name, value);
            } catch (IllegalAccessException e) {
                throw new OrmException("Could not access field " + field.getName(), e);
            }
        }

        executor.save(tableName, fieldMap);
    }

    public Optional<T> findById(ID id) {
        Map<String, Object> fromDB = executor.getById(tableName, id, fields.size());
        if (fromDB.isEmpty()) {
            return Optional.empty();
        }

        T entity = createEntity(fromDB);

        return Optional.of(entity);

    }

    public List<T> findAll() {
        List<Map<String, Object>> fromDB = executor.getAll(tableName, fields.size());
        if (fromDB.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>();

        for (Map<String, Object> rowData : fromDB) {
            T entity = createEntity(rowData);
            result.add(entity);
        }

        return result;
    }

    public void deleteById(ID id) {
        executor.deleteById(tableName, id);
    }

    private T createEntity(Map<String, Object> data) {
        Object newEntity = null;
        try {
            newEntity = clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new OrmException("Failed to create entity object", e);
        }

        for (Entry<String, Object> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            Field field = fields.get(fieldName);
            if (field == null) {
                throw new OrmException("Invalid mapping from db table: field '" + fieldName + "' not found in entity");
            }

            field.setAccessible(true);
            try {
                field.set(newEntity, entry.getValue());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            field.setAccessible(false);
        }

        return (T) newEntity;
    }

    private void inspectClass(Class<?> clazz) {
        Entity entityAnnot = clazz.getAnnotation(Entity.class);
        if (entityAnnot == null) {
            throw new OrmException("Repo works only on @Entity classes");
        }

        Table tableAnnot = clazz.getAnnotation(Table.class);
        if (tableAnnot != null && tableAnnot.name() != null) {
            tableName = tableAnnot.name();
        } else {
            tableName = clazz.getSimpleName().toLowerCase(Locale.ROOT);
        }

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            Column colAnn = field.getAnnotation(Column.class);
            String name = colAnn == null ? field.getName() : colAnn.name();
            fields.put(name, field);
        }
    }
}
