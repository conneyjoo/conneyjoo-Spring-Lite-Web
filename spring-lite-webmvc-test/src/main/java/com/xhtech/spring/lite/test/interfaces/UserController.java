package com.xhtech.spring.lite.test.interfaces;

import com.xhtech.springframework.lite.web.bind.annotation.ResponseAdapter;
import com.xhtech.springframework.lite.web.bind.annotation.ResponseConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController
{
    @GetMapping(path = {"/users/{userId}"})
    @ResponseAdapter(ResponseConverter.MappingJackson2)
    public Long getUser(@PathVariable long userId)
    {
        return userId;
    }

    @GetMapping(path = "/users/{logonName}/mobile/verify")
    public String verifyUsernameWithPhone(@PathVariable String logonName)
    {
        return logonName;
    }

    @RequestMapping("/users")
    @ResponseAdapter(ResponseConverter.MappingJackson2)
    public ResponseEntity<User> users(@RequestBody(required = false) User user)
    {
        return ResponseEntity.ok(user);
    }
}
