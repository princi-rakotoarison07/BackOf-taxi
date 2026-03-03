package testFramework.com.testframework.controller;

import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.PostMapping;
import framework.annotation.SimpleMapping;
import framework.annotation.Param;
import framework.utilitaire.ModelAndView;

@Controller
public class TestControllerWithAnnotations {
    
    @GetMapping("/test")
    public String testMethod() {
        return "Test method";
    }
    
    @GetMapping("/hello")
    public String helloMethod() {
        return "Hello world";
    }
    
    public String methodWithoutAnnotation() {
        return "No annotation";
    }
    
    @GetMapping("/bonjour")
    public String bonjour() {
        return "Bonjour du framework";
    }

    // Page de démonstration qui contient des formulaires GET et POST et un SimpleMapping
    @GetMapping("/methods/form")
    public ModelAndView showMethodsForm() {
        return new ModelAndView("/methodsForm.jsp");
    }

    // GET: lit le paramètre name et l'affiche via ModelAndView
    @GetMapping("/methods")
    public ModelAndView handleGet(@Param("name") String name) {
        return new ModelAndView("/bookView.jsp").addObject("bookId", name);
    }

    // POST: lit le paramètre name et l'affiche via ModelAndView (même vue pour la démo)
    @PostMapping("/methods")
    public ModelAndView handlePost(@Param("name") String name) {
        return new ModelAndView("/bookView2.jsp").addObject("idFromParam", name);
    }

    // SimpleMapping: accepte GET ou POST et renvoie une String simple
    @SimpleMapping("/methods/simple")
    public String simpleAny(@Param("q") String q) {
        return "SimpleMapping ANY => q=" + String.valueOf(q);
    }
}
