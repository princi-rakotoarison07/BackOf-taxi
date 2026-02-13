package testFramework.com.testframework.controller;

import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.utilitaire.ModelAndView;
import framework.utilitaire.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

@Controller
public class FormMapController {

    @GetMapping("/formMap")
    public ModelAndView showForm() {
        return new ModelAndView("/formMap.jsp");
    }

    // Using GET for simplicity; if you add @PostMapping later you can switch the form method to POST
    @GetMapping("/submitMap")
    public ModelAndView submit(HttpServletRequest request) {
        Map<String, Object> params = RequestUtils.buildParamMap(request);
        // Save in session so another endpoint can use the same map
        HttpSession session = request.getSession(true);
        session.setAttribute("lastParams", params);
        ModelAndView mv = new ModelAndView("/mapView.jsp");
        mv.addObject("params", params);
        return mv;
    }

    // Returns another ModelAndView built from the previously stored map
    @GetMapping("/mapSummary")
    public ModelAndView mapSummary(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> params = null;
        if (session != null) {
            Object obj = session.getAttribute("lastParams");
            if (obj instanceof Map) {
                //noinspection unchecked
                params = (Map<String, Object>) obj;
            }
        }
        ModelAndView mv = new ModelAndView("/mapSummary.jsp");
        mv.addObject("params", params);
        return mv;
    }
}
