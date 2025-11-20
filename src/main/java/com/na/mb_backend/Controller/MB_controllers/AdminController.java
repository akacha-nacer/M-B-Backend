package com.na.mb_backend.Controller.MB_controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin")
public class AdminController {

    @GetMapping
    public String get(){
        return "Get :: admin controller";
    }

    @PostMapping
    public String post(){
        return "post :: admin controller";
    }

    @PutMapping
    public String put(){
        return "put :: admin controller";
    }

    @DeleteMapping
    public String delete(){
        return "delete :: admin controller";
    }
}
