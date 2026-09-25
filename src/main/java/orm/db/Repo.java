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
    private EntityInfo entityInfo;

    public Repo(Class<T> clazz) {
        this.executor = new QueryExecutor();
        this.clazz = clazz;
        this.entityInfo = Inspector.getInspector().getEntityInfos().get(clazz.getName());
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

        executor.save(entityInfo.getTableName(), fieldMap);
    }

    public Optional<T> findById(ID id) {
        Map<String, Object> fromDB = executor.getById(entityInfo.getTableName(), id, entityInfo.getFields().size());
        if (fromDB.isEmpty()) {
            return Optional.empty();
        }

        T entity = createEntity(fromDB);

        return Optional.of(entity);

    }

    public List<T> findAll() {
        List<Map<String, Object>> fromDB = executor.getAll(entityInfo.getTableName(), entityInfo.getFields().size());
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
        executor.deleteById(entityInfo.getTableName(), id);
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
            Field field = entityInfo.getFields().get(fieldName);
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
}
