package framework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import framework.annotation.AnnotationReader;
import framework.annotation.Param;
import framework.annotation.ModelAttribute;
import framework.annotation.RestController;
import framework.annotation.Session;
import framework.annotation.Authorized;
import framework.annotation.RequestBody;
import framework.annotation.Role;
import framework.utilitaire.MappingInfo;
import framework.utilitaire.ModelAndView;
import framework.utilitaire.RequestUtils;
import framework.utilitaire.ModelBinder;
import framework.utilitaire.JsonUtils;
import framework.utilitaire.FileUploadUtils;
import framework.utilitaire.UploadedFile;
import framework.utilitaire.SessionMap;
import jakarta.servlet.annotation.MultipartConfig;

@MultipartConfig
public class FrontServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize annotation-based URL mappings once at startup
        AnnotationReader.init();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String originalURI = (String) req.getAttribute("originalURI");
        String requestURI = originalURI != null ? originalURI : req.getRequestURI();
        String contextPath = req.getContextPath();
        String urlPath = requestURI.startsWith(contextPath) 
                ? requestURI.substring(contextPath.length()) 
                : requestURI;
        if (urlPath.isEmpty()) {
            urlPath = "/";
        }

        System.out.println("FrontServlet handling: " + urlPath);

        
        String httpMethod = req.getMethod();
        MappingInfo mapping = AnnotationReader.findMappingByUrl(urlPath, httpMethod);

        
        resp.setContentType("text/html;charset=UTF-8");

        if (mapping == null || !mapping.isFound()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("<h1>404 - URL non trouvée: " + urlPath + "</h1>");
            return;
        }

        try {
            
            if (req.getContentType() != null && req.getContentType().toLowerCase().startsWith("multipart/")) {
                req.setAttribute("uploadedFiles", FileUploadUtils.getUploadedFiles(req));
            }
            
            Map<String, String> vars = mapping.getLastPathVariables();
            if (vars != null) {
                for (Map.Entry<String, String> e : vars.entrySet()) {
                    req.setAttribute(e.getKey(), e.getValue());
                }
            }

            
            Map<String, Object> paramMap = RequestUtils.buildParamMap(req);
            req.setAttribute("params", paramMap);

            Class<?> controllerClass = mapping.getControllerClass();
            boolean isRest = controllerClass.isAnnotationPresent(RestController.class);
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
            Method method = mapping.getMethod();

            
            boolean requiresAuth = controllerClass.isAnnotationPresent(Authorized.class)
                    || method.isAnnotationPresent(Authorized.class);
            if (requiresAuth) {
                String attrName = getServletContext().getInitParameter("auth.session.attribute");
                if (attrName == null || attrName.isEmpty()) {
                    attrName = "user"; 
                }
                HttpSession session = req.getSession(false);
                Object marker = (session != null) ? session.getAttribute(attrName) : null;
                boolean allowed = marker != null && (!(marker instanceof String) || !((String) marker).isEmpty());
                if (!allowed) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.setContentType("text/html;charset=UTF-8");
                    resp.getWriter().write("<h1>401 - Non autorisé</h1><p>Veuillez vous connecter.</p>");
                    return;
                }
            }

            
            Role roleAnn = method.getAnnotation(Role.class);
            if (roleAnn == null) {
                roleAnn = controllerClass.getAnnotation(Role.class);
            }
            if (roleAnn != null) {
                String roleAttr = getServletContext().getInitParameter("role.session.attribute");
                if (roleAttr == null || roleAttr.isEmpty()) {
                    roleAttr = "role"; // default
                }
                HttpSession session = req.getSession(false);
                Object marker = (session != null) ? session.getAttribute(roleAttr) : null;

                // Build a set of current roles from the session attribute
                java.util.Set<String> currentRoles = new java.util.HashSet<>();
                if (marker != null) {
                    if (marker instanceof String) {
                        String s = ((String) marker).trim();
                        if (!s.isEmpty()) {
                            // Support comma-separated roles in a single String
                            for (String part : s.split(",")) {
                                String role = part.trim();
                                if (!role.isEmpty()) currentRoles.add(role.toLowerCase());
                            }
                        }
                    } else if (marker.getClass().isArray()) {
                        int len = java.lang.reflect.Array.getLength(marker);
                        for (int i = 0; i < len; i++) {
                            Object el = java.lang.reflect.Array.get(marker, i);
                            if (el != null) currentRoles.add(String.valueOf(el).toLowerCase());
                        }
                    } else if (marker instanceof java.util.Collection<?>) {
                        for (Object el : (java.util.Collection<?>) marker) {
                            if (el != null) currentRoles.add(String.valueOf(el).toLowerCase());
                        }
                    } else {
                        currentRoles.add(String.valueOf(marker).toLowerCase());
                    }
                }

                boolean ok = false;
                for (String r : roleAnn.value()) {
                    if (r != null && currentRoles.contains(r.toLowerCase())) { ok = true; break; }
                }
                if (!ok) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.setContentType("text/html;charset=UTF-8");
                    resp.getWriter().write("<h1>403 - Accès interdit</h1><p>Rôle requis: " + String.join(", ", roleAnn.value()) + "</p>");
                    return;
                }
            }

            // Build method arguments: support HttpServletRequest/HttpServletResponse and @ModelAttribute binding
            Parameter[] params = method.getParameters();
            Object[] args = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                Class<?> pt = params[i].getType();
                if (HttpServletRequest.class.isAssignableFrom(pt)) {
                    args[i] = req;
                } else if (HttpServletResponse.class.isAssignableFrom(pt)) {
                    args[i] = resp;
                } else if (params[i].isAnnotationPresent(Session.class)) {
                    if (Map.class.isAssignableFrom(pt)) {
                        args[i] = new SessionMap(req.getSession());
                        continue;
                    }
                } else if (params[i].isAnnotationPresent(ModelAttribute.class)) {
                    // Bind a complex object from request parameters
                    args[i] = ModelBinder.bind(req, pt);
                } else if (params[i].isAnnotationPresent(RequestBody.class)) {
                    // Bind from JSON body
                    String body = RequestUtils.getRequestBody(req);
                    args[i] = JsonUtils.fromJson(body, pt);
                } else {
                    // Try to resolve using @Param annotation first
                    Param pAnn = params[i].getAnnotation(Param.class);
                    String raw = null;
                    if (pAnn != null) {
                        String paramName = pAnn.value();
                        // Uploaded file binding support
                        if (UploadedFile.class.isAssignableFrom(pt)) {
                            Object mapObj = req.getAttribute("uploadedFiles");
                            if (mapObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, UploadedFile> fMap = (Map<String, UploadedFile>) mapObj;
                                args[i] = fMap.get(paramName);
                                continue;
                            }
                        }
                        raw = req.getParameter(paramName);
                        if (raw == null) {
                            Object attr = req.getAttribute(paramName);
                            if (attr != null) {
                                if (pt.isInstance(attr)) {
                                    args[i] = attr;
                                    continue;
                                }
                                raw = String.valueOf(attr);
                            }
                        }
                    } else {
                        // Fallback: try parameter name (requires -parameters at compile time) or attribute
                        String guessName = params[i].getName();
                        raw = req.getParameter(guessName);
                        if (raw == null) {
                            Object attr = req.getAttribute(guessName);
                            if (attr != null) {
                                if (pt.isInstance(attr)) {
                                    args[i] = attr;
                                    continue;
                                }
                                raw = String.valueOf(attr);
                            }
                        }
                    }

                    if (raw != null) {
                        args[i] = convertValue(raw, pt);
                    } else {
                        args[i] = pt.isPrimitive() ? defaultForPrimitive(pt) : null;
                    }
                }
            }

            Object result = method.invoke(controllerInstance, args);

            if (result instanceof ModelAndView) {
                ModelAndView mv = (ModelAndView) result;
                // Set model attributes
                for (Map.Entry<String, Object> entry : mv.getModel().entrySet()) {
                    req.setAttribute(entry.getKey(), entry.getValue());
                }
                // Forward to JSP view
                RequestDispatcher dispatcher = req.getRequestDispatcher(mv.getView());
                dispatcher.forward(req, resp);
            } else if (isRest) {
                // Serialize to JSON
                resp.setContentType("application/json;charset=UTF-8");
                String json = JsonUtils.toJson(result);
                resp.getWriter().write(json);
            } else if (result == null) {
                resp.getWriter().write("");
            } else {
                // Default textual rendering
                resp.getWriter().write(String.valueOf(result));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("<h1>500 - Erreur serveur</h1><pre>" + e.getMessage() + "</pre>");
        }
    }

    private Object convertValue(String raw, Class<?> targetType) {
        if (raw == null) return null;
        if (String.class.equals(targetType)) return raw;
        try {
            if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(raw);
            if (targetType == long.class || targetType == Long.class) return Long.parseLong(raw);
            if (targetType == double.class || targetType == Double.class) return Double.parseDouble(raw);
            if (targetType == float.class || targetType == Float.class) return Float.parseFloat(raw);
            if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(raw);
            if (targetType == short.class || targetType == Short.class) return Short.parseShort(raw);
            if (targetType == byte.class || targetType == Byte.class) return Byte.parseByte(raw);
            if (targetType == char.class || targetType == Character.class) return raw.isEmpty() ? '\0' : raw.charAt(0);
            if (targetType == java.sql.Timestamp.class) {
                String normalized = raw.replace("T", " ");
                if (normalized.length() == 16) normalized += ":00";
                return java.sql.Timestamp.valueOf(normalized);
            }
            if (targetType.isEnum()) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Class<? extends Enum> et = (Class<? extends Enum>) targetType;
                return Enum.valueOf(et, raw);
            }
        } catch (Exception ignore) {
            // fall through to return null/default
        }
        return null;
    }

    private Object defaultForPrimitive(Class<?> pt) {
        if (pt == boolean.class) return false;
        if (pt == char.class) return '\0';
        if (pt == byte.class) return (byte) 0;
        if (pt == short.class) return (short) 0;
        if (pt == int.class) return 0;
        if (pt == long.class) return 0L;
        if (pt == float.class) return 0f;
        if (pt == double.class) return 0d;
        return null;
    }
}
