package uz.master.demotest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class BaseController {

    @RequestMapping(value = {"/home", "/"})
    public String home() {
        return "/project/all";
    }

    @RequestMapping(value = {"/contacts"})
    public String contacts() {
        return "contatcs";
    }

    @PostMapping(value = {"/search"})
    public String search() {
        return "search";
    }
}
