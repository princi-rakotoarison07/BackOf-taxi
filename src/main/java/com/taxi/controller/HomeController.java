package com.taxi.controller;

import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.utilitaire.ModelAndView;

@Controller
public class HomeController {

    @GetMapping("/")
    public ModelAndView root() {
        ModelAndView mv = new ModelAndView("/BackOf-taxi/");
        mv.setRedirect(true);
        return mv;
    }

    @GetMapping("/BackOf-taxi/")
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView("/views/home.jsp");
        mv.addObject("pageTitle", "Accueil");
        return mv;
    }
}
