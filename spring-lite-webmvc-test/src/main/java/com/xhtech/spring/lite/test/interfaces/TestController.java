package com.xhtech.spring.lite.test.interfaces;

import com.xhtech.springframework.lite.web.bind.annotation.ResponseAdapter;
import com.xhtech.springframework.lite.web.bind.annotation.ResponseConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

@RestController
@RequestMapping("/test")
public class TestController
{
    @GetMapping(path = "/box/server/{userId}/download")
    public Long download(@PathVariable Long userId, HttpServletRequest request)
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
        return String.valueOf(new Random().nextInt(100));
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

    @GetMapping("/box/server/**/file/download/{userId}/**/{appId}")
    public Long download66(@PathVariable Long userId, @PathVariable String appId, HttpServletRequest request)
    {
        return userId;
    }

    @GetMapping("/box/server/**/file/download/{userId}/**/ss/{appId}")
    @ResponseAdapter(ResponseConverter.String)
    public String download666(@PathVariable Long userId, @PathVariable String appId, HttpServletRequest request)
    {
        return appId;
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
    @ResponseAdapter(ResponseConverter.String)
    public String request(@PathVariable Long userId, @RequestParam String url, HttpServletRequest request)
    {
        return url;
    }

    @RequestMapping("/box/server/{userId}/request2")
    public String request2(@PathVariable Long userId, @RequestParam String url, HttpServletRequest request)
    {
        return url;
    }

    @GetMapping("/box/server/test")
    public ResponseEntity<String> test(@RequestParam String url, HttpServletRequest request)
    {
        if (url.equals("1"))
        {
            throw new IllegalArgumentException();
        }
        return ResponseEntity.ok(url);
    }

    @GetMapping("/box/server/json")
    @ResponseAdapter(ResponseConverter.MappingJackson2)
    public @ResponseBody User json(@RequestParam String url)
    {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123");
        return user;
    }

    @GetMapping("/box/server/json2")
    @ResponseAdapter(ResponseConverter.MappingJackson2)
    public @ResponseBody User json2()
    {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123");
        return user;
    }

    @GetMapping("/box/server/json3")
    public @ResponseBody User json3()
    {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123");
        return user;
    }

    @GetMapping("/box/server/json4")
    @ResponseAdapter(ResponseConverter.MappingJackson2)
    public ResponseEntity<User> json4()
    {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123");
        return ResponseEntity.ok(user);
    }

    @GetMapping("/box/server/json5")
    public ResponseEntity<User> json5()
    {
        User user = new User();
        user.setUsername("test5");
        user.setPassword("123");
        return ResponseEntity.ok(user);
    }

    @GetMapping("/box/server/json6")
    public ResponseEntity<Void> json6() throws URISyntaxException
    {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123");
        return ResponseEntity.created(new URI("/test/box/server/json6")).build();
    }

    @GetMapping("/box/server/e1")
    public ResponseEntity<byte[]> e1()
    {
        return ResponseEntity.ok(new byte[]{1,2,3,4,5});
    }

    @GetMapping("/box/server/e2")
    public byte[] e2()
    {
        return new byte[]{1,2,3,4,5};
    }

    @GetMapping("/box/server/e3")
    @ResponseAdapter(ResponseConverter.ByteArray)
    public byte[] e3()
    {
        return new byte[]{1,2,3,4,5};
    }

    @GetMapping("/box/server/void")
    public void vd(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("text/html");
        response.setStatus(HttpStatus.OK.value());
        ServletOutputStream os = response.getOutputStream();
        os.write(1);
        os.flush();
        os.close();
    }
}
