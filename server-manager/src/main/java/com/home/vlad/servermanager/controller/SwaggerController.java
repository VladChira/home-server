package com.home.vlad.servermanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SwaggerController {
    @RequestMapping("/manage/docs")
    public String getRedirectUrl() {
        return "redirect:/manage/swagger-ui/index.html";
    }
}
