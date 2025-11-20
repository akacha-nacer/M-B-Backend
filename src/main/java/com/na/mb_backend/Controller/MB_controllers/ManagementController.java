package com.na.mb_backend.Controller.MB_controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/management")
public class ManagementController {

    @GetMapping
    public String get(){
        return "Get :: management controller";
    }

    @PostMapping
    public String post(){
        return "post :: management controller";
    }

    @PutMapping
    public String put(){
        return "put :: management controller";
    }

    @DeleteMapping
    public String delete(){
        return "delete :: management controller";
    }
}
