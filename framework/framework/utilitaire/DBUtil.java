package framework.utilitaire;

import framework.annotation.Column;
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
            if (i < columns.size() - 1) sql.append(", ");
        }
        sql.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            sql.append("?");
            if (i < columns.size() - 1) sql.append(", ");
        }
        sql.append(")");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
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
                        field.set(instance, value);
                    }
                }
                results.add(instance);
            }
        }
        return results;
    }
}
