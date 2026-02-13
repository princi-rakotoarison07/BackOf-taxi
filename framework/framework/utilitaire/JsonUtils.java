package framework.utilitaire;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtils {
    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof Number || obj instanceof Boolean) return String.valueOf(obj);
        if (obj instanceof String) return quote((String) obj);
        if (obj instanceof java.sql.Timestamp || obj instanceof java.util.Date) {
            return quote(obj.toString());
        }
        if (obj.getClass().isArray()) return arrayToJson(obj);
        if (obj instanceof Collection) return collectionToJson((Collection<?>) obj);
        if (obj instanceof Map) return mapToJson((Map<?, ?>) obj);
        return objectToJson(obj);
    }

    /**
     * Parseur JSON très simplifié pour objets plats.
     * Supporte: {"key": "value", "key2": 123, "key3": true}
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Map<String, String> map = parseSimpleJson(json);
            
            for (Map.Entry<String, String> entry : map.entrySet()) {
                try {
                    Field field = findField(clazz, entry.getKey());
                    if (field != null) {
                        field.setAccessible(true);
                        field.set(instance, convert(entry.getValue(), field.getType()));
                    }
                } catch (Exception ignore) {}
            }
            return instance;
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        // Regex pour extraire "key": value (value peut être string entre "" ou nombre/bool)
        Pattern pattern = Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"[^\"]*\"|[^,}]+)");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            map.put(key, value);
        }
        return map;
    }

    private static Field findField(Class<?> clazz, String name) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private static Object convert(String value, Class<?> type) {
        if (value == null || value.equals("null")) return null;
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        return null;
    }

    private static String quote(String s) {
        if (s == null) return "null";
        return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + '"';
    }

    private static String arrayToJson(Object array) {
        int len = Array.getLength(array);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(',');
            sb.append(toJson(Array.get(array, i)));
        }
        sb.append(']');
        return sb.toString();
    }

    private static String collectionToJson(Collection<?> coll) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object o : coll) {
            if (!first) sb.append(',');
            first = false;
            sb.append(toJson(o));
        }
        sb.append(']');
        return sb.toString();
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(quote(String.valueOf(e.getKey()))).append(':').append(toJson(e.getValue()));
        }
        sb.append('}');
        return sb.toString();
    }

    private static String objectToJson(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        Class<?> cls = obj.getClass();
        while (cls != null && cls != Object.class) {
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                try {
                    Object v = f.get(obj);
                    if (!first) sb.append(',');
                    first = false;
                    sb.append(quote(f.getName())).append(':').append(toJson(v));
                } catch (IllegalAccessException ignore) {}
            }
            cls = cls.getSuperclass();
        }
        sb.append('}');
        return sb.toString();
    }
}
