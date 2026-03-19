package framework.utilitaire;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;

public class ModelBinder {
    public static Object bind(HttpServletRequest request, Class<?> targetType) {
        try {
            Object instance = targetType.getDeclaredConstructor().newInstance();
            // First pass: flat properties matching direct names (for convenience)
            bindFlat(request, instance, targetType);

            // Second pass: nested properties using dotted names, e.g., dept.id
            Enumeration<String> names = request.getParameterNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                if (name.contains(".")) {
                    String raw = request.getParameter(name);
                    if (raw == null) continue;
                    setPropertyPath(instance, targetType, name.split("\\."), 0, raw);
                }
            }
            return instance;
        } catch (Exception e) {
            return null;
        }
    }

    private static void bindFlat(HttpServletRequest request, Object instance, Class<?> type) {
        // Bind by setters first
        Method[] methods = type.getMethods();
        for (Method m : methods) {
            if (isSetter(m)) {
                String prop = decapitalize(m.getName().substring(3));
                String raw = request.getParameter(prop);
                if (raw != null) {
                    Class<?> paramType = m.getParameterTypes()[0];
                    Object converted = convert(raw, paramType);
                    try { m.invoke(instance, converted); } catch (Exception ignore) {}
                }
            }
        }
        // Fallback: direct fields
        Field[] fields = type.getDeclaredFields();
        for (Field f : fields) {
            String name = f.getName();
            String raw = request.getParameter(name);
            if (raw != null) {
                Object converted = convert(raw, f.getType());
                try {
                    f.setAccessible(true);
                    f.set(instance, converted);
                } catch (Exception ignore) {}
            }
        }
    }

    private static void setPropertyPath(Object root, Class<?> rootType, String[] parts, int idx, String rawValue) {
        try {
            String prop = parts[idx];
            boolean last = idx == parts.length - 1;
            if (last) {
                Class<?> propType = getPropertyType(rootType, prop);
                Object converted = convert(rawValue, propType);
                setPropertyValue(root, prop, converted);
                return;
            }
            // Intermediate: get or create nested object
            Object current = getPropertyValue(root, prop);
            Class<?> propType = getPropertyType(rootType, prop);
            if (current == null && propType != null) {
                try {
                    current = propType.getDeclaredConstructor().newInstance();
                    setPropertyValue(root, prop, current);
                } catch (Exception ignore) { return; }
            }
            if (current != null) {
                setPropertyPath(current, current.getClass(), parts, idx + 1, rawValue);
            }
        } catch (Exception ignore) {
        }
    }

    private static boolean isSetter(Method m) {
        return m.getName().startsWith("set") && m.getParameterCount() == 1;
    }

    private static String decapitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static Class<?> getPropertyType(Class<?> type, String prop) {
        // Try setter
        String setter = "set" + Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
        for (Method m : type.getMethods()) {
            if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                return m.getParameterTypes()[0];
            }
        }
        // Try field
        try {
            Field f = type.getDeclaredField(prop);
            return f.getType();
        } catch (NoSuchFieldException ignore) {}
        return String.class;
    }

    private static Object getPropertyValue(Object obj, String prop) {
        // Try getter
        String getter = "get" + Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
        try {
            Method gm = obj.getClass().getMethod(getter);
            return gm.invoke(obj);
        } catch (Exception ignore) {}
        // Try field
        try {
            Field f = obj.getClass().getDeclaredField(prop);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception ignore) {}
        return null;
    }

    private static void setPropertyValue(Object obj, String prop, Object value) {
        // Try setter
        String setter = "set" + Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                try { m.invoke(obj, value); return; } catch (Exception ignore) {}
            }
        }
        // Try field
        try {
            Field f = obj.getClass().getDeclaredField(prop);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception ignore) {}
    }

    private static Object convert(String raw, Class<?> type) {
        if (raw == null) return null;
        if (type == String.class) return raw;
        try {
            if (type == int.class || type == Integer.class) return Integer.parseInt(raw);
            if (type == long.class || type == Long.class) return Long.parseLong(raw);
            if (type == double.class || type == Double.class) return Double.parseDouble(raw);
            if (type == float.class || type == Float.class) return Float.parseFloat(raw);
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(raw);
            if (type == short.class || type == Short.class) return Short.parseShort(raw);
            if (type == byte.class || type == Byte.class) return Byte.parseByte(raw);
            if (type == char.class || type == Character.class) return raw.isEmpty() ? '\0' : raw.charAt(0);
            if (type == java.sql.Timestamp.class) {
                // Handle standard format or HTML5 datetime-local (T instead of space)
                String normalized = raw.replace("T", " ");
                if (normalized.length() == 16) { // yyyy-MM-dd HH:mm
                    normalized += ":00";
                }
                return java.sql.Timestamp.valueOf(normalized);
            }
            if (type.isEnum()) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Class<? extends Enum> et = (Class<? extends Enum>) type;
                return Enum.valueOf(et, raw);
            }
        } catch (Exception ignore) {}
        return null;
    }
}
