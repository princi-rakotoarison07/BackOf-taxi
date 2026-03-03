package testFramework.com.testframework.controller;

import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.PostMapping;
import framework.annotation.ModelAttribute;
import framework.utilitaire.ModelAndView;
import testFramework.com.testframework.model.Employe;

@Controller
public class EmployeController {

    @GetMapping("/employe/form")
    public ModelAndView form() {
        return new ModelAndView("/employeForm.jsp");
    }

    @PostMapping("/employe/save")
    public ModelAndView save(@ModelAttribute Employe emp) {
        ModelAndView mv = new ModelAndView("/employeView.jsp");
        mv.addObject("emp", emp);
        return mv;
    }
}
