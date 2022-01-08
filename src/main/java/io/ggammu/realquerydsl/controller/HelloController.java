package io.ggammu.realquerydsl.controller;

import io.ggammu.realquerydsl.entity.Team;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hell() {
        return "hello";
    }

}
