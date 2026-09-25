package orm.db;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import orm.annot.Column;
import orm.annot.Table;

public class Inspector {

    private Set<String> classPathes;
    private Set<Class> allClasses;
    private Map<String, EntityInfo> entityInfos;
    private static Inspector inspector;

    protected static Inspector getInspector() {
        if (inspector == null) {
            inspector = new Inspector();
        }
        return inspector;
    }

    private Inspector() {
        this.classPathes = new HashSet<>();
        this.allClasses = new HashSet<>();
        this.entityInfos = new HashMap<>();
    }

    public void inspect(String mainPath) {
        getAllClasses(mainPath);
        getAllEntities();
    }

    private void getAllEntities() {
        for (Class<?> clazz : allClasses) {
            Table annot = clazz.getAnnotation(Table.class);
            if (annot == null) {
                continue;
            }

            EntityInfo info = new EntityInfo();
            String tableName = annot.name() == null ? clazz.getSimpleName() : annot.name();
            Map<String, Field> fields = new HashMap<>();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                Column colAnn = field.getAnnotation(Column.class);
                String name = colAnn == null ? field.getName() : colAnn.name();
                fields.put(name, field);
            }

            info.setFields(fields);
            info.setTableName(tableName);

            entityInfos.put(clazz.getName(), info);
        }
    }

    private void getAllClasses(String mainPath) {
        int basePathIndex = mainPath.indexOf("/target/classes/");
        String basePath = mainPath.substring(5, basePathIndex + 15);
        File baseDir = new File(basePath);

        findClasses(baseDir);
        Set<String> normalClasses =  normalizeClassNames();

        try {
            for (String className : normalClasses) {
                Class clazz = Class.forName(className);
                allClasses.add(clazz);
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    private void findClasses(File baseDir) {
        for (File file : baseDir.listFiles()) {
            if (file.getName().endsWith(".class")) {
                classPathes.add(file.getPath());
            } else if (file.isDirectory()) {
                findClasses(file);
            }
        }
    }

    private Set<String> normalizeClassNames() {
        Set<String> result = new HashSet<>();
        for (String classPath : classPathes) {
            int pathIndex = classPath.indexOf("/target/classes/");
            String path = classPath.substring(pathIndex + 16);
            path = path.replace('/', '.');
            path = path.substring(0, path.length() - 6);
            result.add(path);
            System.out.println(path);
        }
        return result;
    }


    protected Map<String, EntityInfo> getEntityInfos() {
        return this.entityInfos;
    }

}
