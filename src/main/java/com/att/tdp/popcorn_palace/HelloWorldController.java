package com.att.tdp.popcorn_palace;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelloWorldController {

    // This method maps to the root URL ("/")
    @GetMapping("/hello")
    public String helloWorld() {

        return "hello";  // Return the "hello" view
    }
}