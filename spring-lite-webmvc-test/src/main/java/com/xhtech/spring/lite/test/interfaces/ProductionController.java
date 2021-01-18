package com.xhtech.spring.lite.test.interfaces;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ProductionController
{
    @CrossOrigin
    @GetMapping(path = {"/production/orders"})
    public String orders()
    {
        return "La campanella";
    }
}
