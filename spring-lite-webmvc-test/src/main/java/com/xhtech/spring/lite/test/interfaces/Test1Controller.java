package com.xhtech.spring.lite.test.interfaces;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/test1")
public class Test1Controller
{
    @GetMapping(path = "/box/server/{userId}/download")
    public Long download(@PathVariable Long userId, @RequestParam String url, HttpServletRequest request)
    {
        return userId;
    }

    @GetMapping(path = "/box/server/*/download1")
    public String download1(HttpServletRequest request)
    {
        return "*";
    }

    @GetMapping(path = "/box/server/*/{userId}")
    public Long download2(@PathVariable Long userId, HttpServletRequest request)
    {
        return userId;
    }

    @GetMapping(path = "/box/server/{appId}/{userId}")
    public Long download21(@PathVariable Long appId, @PathVariable Long userId, HttpServletRequest request)
    {
        return appId + userId;
    }

    @GetMapping(path = "/box/server/download")
    public String download3(HttpServletRequest request)
    {
        return "3";
    }

    @PostMapping(path = "/box/server/download")
    public String download3a(HttpServletRequest request)
    {
        return "3a";
    }

    @GetMapping("/box/server/{value}")
    public String download4(HttpServletRequest request)
    {
        return "4";
    }

    @GetMapping("/box/server/**/file/download")
    public String download5(HttpServletRequest request)
    {
        return "5";
    }

    @GetMapping("/box/server/**/file/download/test")
    public String download6(HttpServletRequest request)
    {
        return "6";
    }

    @GetMapping("/box/server/**/file/download/{userId}")
    public Long download6(@PathVariable Long userId, HttpServletRequest request)
    {
        return userId;
    }

    @GetMapping("/box/server/aaa/file/**/download/test")
    public String download7(HttpServletRequest request)
    {
        return "7";
    }

    @GetMapping("/box/server/aaa/file/**")
    public String download8(HttpServletRequest request)
    {
        return "8";
    }

    @GetMapping("/box/server/{userId}/{schoolId}/{appId}")
    public String download9(HttpServletRequest request)
    {
        return "9";
    }

    @GetMapping("/box/server/*/{schoolId}/*/{appId}")
    public String download10(HttpServletRequest request)
    {
        return "10";
    }

    @RequestMapping("/box/server/{userId}/request")
    public String request(@PathVariable Long userId, @RequestParam String url, HttpServletRequest request)
    {
        return url;
    }
}
