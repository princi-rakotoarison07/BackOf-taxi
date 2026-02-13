package testFramework.com.testframework.controller;

import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.Param;
import framework.annotation.Session;
import framework.annotation.Authorized;
import framework.annotation.Role;
import framework.utilitaire.ModelAndView;

import java.util.Map;

@Controller
public class SessionDemoController {

    @GetMapping("/sessionForm")
    public ModelAndView form() {
        return new ModelAndView("/sessionForm.jsp");
    }

    @GetMapping("/sessionSet")
    public ModelAndView set(@Session Map<String, Object> session,
                            @Param("key") String key,
                            @Param("value") String value) {
        if (key != null && !key.trim().isEmpty()) {
            session.put(key, value);
        }
        ModelAndView mv = new ModelAndView("/sessionView.jsp");
        mv.addObject("sessionMap", session);
        mv.addObject("message", "Clé enregistrée dans la session");
        return mv;
    }

    @Authorized
    @Role({"admin","manager"})
    @GetMapping("/sessionShow")
    public ModelAndView show(@Session Map<String, Object> session) {
        ModelAndView mv = new ModelAndView("/sessionView.jsp");
        mv.addObject("sessionMap", session);
        return mv;
    }

    @Authorized
    @Role({"admin","manager"})
    @GetMapping("/sessionRemove")
    public ModelAndView remove(@Session Map<String, Object> session,
                               @Param("key") String key) {
        if (key != null && !key.trim().isEmpty()) {
            session.remove(key);
        }
        ModelAndView mv = new ModelAndView("/sessionView.jsp");
        mv.addObject("sessionMap", session);
        mv.addObject("message", "Clé supprimée si elle existait");
        return mv;
    }

    @Authorized
    @Role({"admin","manager"})
    @GetMapping("/sessionClear")
    public ModelAndView clear(@Session Map<String, Object> session) {
        session.clear();
        ModelAndView mv = new ModelAndView("/sessionView.jsp");
        mv.addObject("sessionMap", session);
        mv.addObject("message", "Session vidée");
        return mv;
    }
}
