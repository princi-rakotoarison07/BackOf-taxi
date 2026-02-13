package framework.utilitaire;

import framework.annotation.GetMapping;
import framework.annotation.PostMapping;
import framework.annotation.SimpleMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.List;
import java.util.Collection;

/**
 * Responsable de la gestion du registre des mappings URL -> Classe/Méthode
 * Principe de Responsabilité Unique (SRP)
 */
public class UrlMappingRegistry {
    
    private List<MappingInfo> urlMappings;
    private boolean initialized;
    
    public UrlMappingRegistry() {
        this.urlMappings = new ArrayList<>();
        this.initialized = false;
    }
    
    /**
     * Construit le registre des URLs à partir des classes scannées
     * @param classes Liste des classes avec @Controller
     */
    public void buildRegistry(List<Class<?>> classes) {
        if (initialized) {
            System.out.println("Registre déjà initialisé.");
            return;
        }
        
        urlMappings.clear();
        int urlCount = 0;
        
        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            
            for (Method method : methods) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    GetMapping mapping = method.getAnnotation(GetMapping.class);
                    String url = mapping.value();
                    MappingInfo mi = new MappingInfo(clazz, method, url, "GET");
                    urlMappings.add(mi);
                    System.out.println("[UrlMappingRegistry] Registered GET: " + url + " -> " + clazz.getSimpleName() + "." + method.getName());
                    urlCount++;
                }
                if (method.isAnnotationPresent(PostMapping.class)) {
                    PostMapping mapping = method.getAnnotation(PostMapping.class);
                    String url = mapping.value();
                    MappingInfo mi = new MappingInfo(clazz, method, url, "POST");
                    urlMappings.add(mi);
                    System.out.println("[UrlMappingRegistry] Registered POST: " + url + " -> " + clazz.getSimpleName() + "." + method.getName());
                    urlCount++;
                }
                if (method.isAnnotationPresent(SimpleMapping.class)) {
                    SimpleMapping mapping = method.getAnnotation(SimpleMapping.class);
                    String url = mapping.value();
                    MappingInfo mi = new MappingInfo(clazz, method, url, "ANY");
                    urlMappings.add(mi);
                    System.out.println("[UrlMappingRegistry] Registered SIMPLE: " + url + " -> " + clazz.getSimpleName() + "." + method.getName());
                    urlCount++;
                }
            }
        }
        
        initialized = true;
        System.out.println("Registre construit: " + urlCount + " URL(s) mappée(s).\n");
    }
    
    /**
     * Recherche un mapping par URL
     * @param url L'URL à rechercher
     * @param httpMethod Méthode HTTP (GET/POST)
     * @return MappingInfo ou null si non trouvé
     */
    public MappingInfo findByUrl(String url, String httpMethod) {
        // Parcours de tous les mappings pour matcher URL + méthode HTTP
        for (MappingInfo mi : urlMappings) {
            if (mi.matches(url)) {
                String allowed = mi.getHttpMethod();
                if ("ANY".equals(allowed) || allowed.equalsIgnoreCase(httpMethod)) {
                    System.out.println("[UrlMappingRegistry] Match: [" + httpMethod + "] " + mi.getUrl() + " ~ " + url);
                    return mi;
                }
            }
        }
        return null;
    }
    
    /**
     * Vérifie si le registre est initialisé
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Retourne le nombre d'URLs enregistrées
     */
    public int size() {
        return urlMappings.size();
    }
}

