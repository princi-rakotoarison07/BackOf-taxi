package framework.utilitaire;

import framework.annotation.Column;
import framework.annotation.Id;
import framework.annotation.Table;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class DBUtil {

    public static void insert(Object obj, Connection conn) throws Exception {
        Class<?> clazz = obj.getClass();

        // Trouver le nom de la table
        String tableName = clazz.getSimpleName();
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            if (!table.name().isEmpty()) {
                tableName = table.name();
            }
        }

        Field[] fields = clazz.getDeclaredFields();
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true);
            String columnName = field.getName();
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (!column.name().isEmpty()) {
                    columnName = column.name();
                }
            }

            columns.add(columnName);
            values.add(field.get(obj));
        }

        if (columns.isEmpty()) {
            throw new Exception("Aucun champ trouvé pour l'insertion dans la table " + tableName);
        }

        // Construire la requête SQL
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i));
            if (i < columns.size() - 1)
                sql.append(", ");
        }
        sql.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            sql.append("?");
            if (i < columns.size() - 1)
                sql.append(", ");
        }
        sql.append(")");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.executeUpdate();
        }
    }

    public static void update(Object obj, Connection conn) throws Exception {
        Class<?> clazz = obj.getClass();

        // Trouver le nom de la table
        String tableName = clazz.getSimpleName();
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            if (!table.name().isEmpty()) {
                tableName = table.name();
            }
        }

        Field[] fields = clazz.getDeclaredFields();
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        String idColumn = null;
        Object idValue = null;

        for (Field field : fields) {
            field.setAccessible(true);
            String columnName = field.getName();
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (!column.name().isEmpty()) {
                    columnName = column.name();
                }
            }

            if (field.isAnnotationPresent(Id.class)) {
                idColumn = columnName;
                idValue = field.get(obj);
            } else {
                columns.add(columnName);
                values.add(field.get(obj));
            }
        }

        if (idColumn == null) {
            throw new Exception("Aucun champ marqué @Id trouvé pour la mise à jour dans la table " + tableName);
        }
        if (columns.isEmpty()) {
            throw new Exception("Aucun champ à mettre à jour trouvé dans la table " + tableName);
        }

        // Construire la requête SQL
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName).append(" SET ");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i)).append(" = ?");
            if (i < columns.size() - 1)
                sql.append(", ");
        }
        sql.append(" WHERE ").append(idColumn).append(" = ?");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int i = 0;
            for (; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.setObject(i + 1, idValue);
            stmt.executeUpdate();
        }
    }

    public static <T> List<T> getAll(Class<T> clazz, Connection conn) throws Exception {
        String tableName = clazz.getSimpleName();
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            if (!table.name().isEmpty()) {
                tableName = table.name();
            }
        }

        String sql = "SELECT * FROM " + tableName;
        List<T> results = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                java.sql.ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                T instance = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String columnName = field.getName();
                    if (field.isAnnotationPresent(Column.class)) {
                        Column column = field.getAnnotation(Column.class);
                        if (!column.name().isEmpty()) {
                            columnName = column.name();
                        }
                    }
                    Object value = rs.getObject(columnName);
                    if (value != null) {
                        field.set(instance, convertValue(value, field.getType()));
                    }
                }
                results.add(instance);
            }
        }
        return results;
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isAssignableFrom(value.getClass())) return value;

        // Conversion java.sql.Date -> java.sql.Timestamp
        if (value instanceof java.sql.Date && targetType == java.sql.Timestamp.class) {
            return new java.sql.Timestamp(((java.sql.Date) value).getTime());
        }
        
        // Conversion java.sql.Timestamp -> java.sql.Date
        if (value instanceof java.sql.Timestamp && targetType == java.sql.Date.class) {
            return new java.sql.Date(((java.sql.Timestamp) value).getTime());
        }

        // Autres conversions si nécessaire (ex: String -> Integer)
        return value;
    }

    public static <T> T getById(Class<T> clazz, Object id, Connection conn) throws Exception {
        String tableName = clazz.getSimpleName();
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            if (!table.name().isEmpty()) {
                tableName = table.name();
            }
        }

        String idColumn = null;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                idColumn = field.getName();
                if (field.isAnnotationPresent(Column.class)) {
                    Column col = field.getAnnotation(Column.class);
                    if (!col.name().isEmpty()) {
                        idColumn = col.name();
                    }
                }
                break;
            }
        }

        if (idColumn == null) {
            throw new Exception("Aucun champ marqué @Id trouvé dans la classe " + clazz.getName());
        }

        String sql = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    T instance = clazz.getDeclaredConstructor().newInstance();
                    for (Field field : clazz.getDeclaredFields()) {
                        field.setAccessible(true);
                        String columnName = field.getName();
                        if (field.isAnnotationPresent(Column.class)) {
                            Column column = field.getAnnotation(Column.class);
                            if (!column.name().isEmpty()) {
                                columnName = column.name();
                            }
                        }
                        Object value = rs.getObject(columnName);
                        if (value != null) {
                            field.set(instance, value);
                        }
                    }
                    return instance;
                }
            }
        }
        return null;
    }

    public static void delete(Object obj, Connection conn) throws Exception {
        Class<?> clazz = obj.getClass();
        String tableName = clazz.getSimpleName();
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            if (!table.name().isEmpty()) {
                tableName = table.name();
            }
        }

        String idColumn = null;
        Object idValue = null;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                idColumn = field.getName();
                if (field.isAnnotationPresent(Column.class)) {
                    Column col = field.getAnnotation(Column.class);
                    if (!col.name().isEmpty()) {
                        idColumn = col.name();
                    }
                }
                idValue = field.get(obj);
                break;
            }
        }

        if (idColumn == null) {
            throw new Exception("Aucun champ marqué @Id trouvé pour la suppression dans " + clazz.getName());
        }

        String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, idValue);
            stmt.executeUpdate();
        }
    }

    public static void deleteById(Class<?> clazz, Object id, Connection conn) throws Exception {
        String tableName = clazz.getSimpleName();
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            if (!table.name().isEmpty()) {
                tableName = table.name();
            }
        }

        String idColumn = null;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                idColumn = field.getName();
                if (field.isAnnotationPresent(Column.class)) {
                    Column col = field.getAnnotation(Column.class);
                    if (!col.name().isEmpty()) {
                        idColumn = col.name();
                    }
                }
                break;
            }
        }

        if (idColumn == null) {
            throw new Exception("Aucun champ marqué @Id trouvé pour la suppression dans " + clazz.getName());
        }

        String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }
}
