package com.vapps.auth.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class FrontController {

    @GetMapping(
        value = { "/", "/{x:[\\w\\-]+}", "/{x:^(?!api$).*$}/*/{y:[\\w\\-]+}", "/error"  }
    )
    public String getReactPage(HttpServletRequest request) {
        return "/index.html";
    }

}
