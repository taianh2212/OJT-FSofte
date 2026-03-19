package com.tourbooking.booking.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "redirect:/pages/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/pages/auth/login.html";
    }
}
