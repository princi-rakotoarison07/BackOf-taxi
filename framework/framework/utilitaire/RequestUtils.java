package framework.utilitaire;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class RequestUtils {
    /**
     * Lit le corps de la requête (body) en tant que String.
     */
    public static String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
    /**
     * Build a Map<String, Object> from request parameters. Values are Strings when
     * possible; if a parameter has multiple values or a non-String type, the value is null
     * according to the requirement.
     */
    public static Map<String, Object> buildParamMap(HttpServletRequest request) {
        if (request == null) return Collections.emptyMap();
        Map<String, Object> result = new HashMap<>();

        // Use getParameterNames so we can read single value easily
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String[] values = request.getParameterValues(name);
            if (values == null) {
                result.put(name, null);
            } else if (values.length == 1) {
                // Single value -> keep as String
                result.put(name, values[0]);
            } else {
                // Multiple values -> considered non-String -> set to null per requirement
                result.put(name, null);
            }
        }
        return result;
    }
}
