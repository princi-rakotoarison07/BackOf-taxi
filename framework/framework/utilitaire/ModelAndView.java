package framework.utilitaire;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String view; // JSP path relative to the webapp root, e.g., "/departements.jsp"
    private Map<String, Object> model = new HashMap<>();
    private boolean isRedirect = false;

    public ModelAndView(String view) {
        this.view = view;
    }

    public ModelAndView(String view, Map<String, Object> model) {
        this.view = view;
        if (model != null) {
            this.model.putAll(model);
        }
    }

    public String getView() {
        return view;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public boolean isRedirect() {
        return isRedirect;
    }

    public void setRedirect(boolean redirect) {
        isRedirect = redirect;
    }

    public ModelAndView addObject(String key, Object value) {
        model.put(key, value);
        return this;
    }
}
