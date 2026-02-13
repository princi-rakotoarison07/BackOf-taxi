package framework.utilitaire;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingInfo {
    private Class<?> controllerClass;
    private Method method;
    private String url;
    private String httpMethod; // "GET", "POST", or "ANY"
    private boolean found;
    private boolean isPattern;
    private Pattern regex;
    private List<String> variableNames;
    private Map<String, String> lastPathVariables;

    public MappingInfo(Class<?> controllerClass, Method method, String url, String httpMethod) {
        this.controllerClass = controllerClass;
        this.method = method;
        this.url = url;
        this.httpMethod = httpMethod;
        this.found = true;
        this.variableNames = new ArrayList<>();
        this.lastPathVariables = new HashMap<>();
        compilePatternIfNeeded();
    }

    public MappingInfo() {
        this.found = false;
        this.variableNames = new ArrayList<>();
        this.lastPathVariables = new HashMap<>();
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public boolean isFound() {
        return found;
    }

    public String getClassName() {
        return found ? controllerClass.getSimpleName() : null;
    }

    public String getMethodName() {
        return found ? method.getName() : null;
    }

    private void compilePatternIfNeeded() {
        if (url != null && (url.contains("{") || url.contains("{{"))) {
            isPattern = true;
            StringBuilder regexBuilder = new StringBuilder("^");
            StringBuilder name = new StringBuilder();
            boolean inVar = false;
            boolean doubleCurly = false;
            for (int i = 0; i < url.length(); i++) {
                char c = url.charAt(i);
                if (!inVar && c == '{') {
                    if (i + 1 < url.length() && url.charAt(i + 1) == '{') {
                        doubleCurly = true;
                        i++; 
                    } else {
                        doubleCurly = false;
                    }
                    inVar = true;
                    name.setLength(0);
                } else if (inVar && c == '}') {
                    if (doubleCurly) {
                        if (i + 1 < url.length() && url.charAt(i + 1) == '}') {
                            i++; 
                        }
                    }
                    inVar = false;
                    variableNames.add(name.toString());
                    regexBuilder.append("([^/]+)");
                } else {
                    if (inVar) {
                        name.append(c);
                    } else {
                        if (".[]()\\+^$|".indexOf(c) >= 0) {
                            regexBuilder.append('\\');
                        }
                        regexBuilder.append(c);
                    }
                }
            }
            regexBuilder.append("$");
            regex = Pattern.compile(regexBuilder.toString());
        } else {
            isPattern = false;
        }
    }

    public boolean matches(String path) {
        if (!isPattern) {
            return url != null && url.equals(path);
        }
        Matcher m = regex.matcher(path);
        if (!m.matches()) return false;
        lastPathVariables.clear();
        for (int i = 0; i < variableNames.size(); i++) {
            lastPathVariables.put(variableNames.get(i), m.group(i + 1));
        }
        return true;
    }

    public Map<String, String> getLastPathVariables() {
        return lastPathVariables;
    }
}
