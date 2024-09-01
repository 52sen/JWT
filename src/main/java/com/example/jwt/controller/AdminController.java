package com.example.jwt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
//웹으로 응답하는 것이 아니기떄문에 특정한 문자열로 지정
public class AdminController {
    @GetMapping
    public String adminP(){
        return "Admin Controller";
    }
}
