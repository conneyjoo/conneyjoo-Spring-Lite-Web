package com.xhtech.spring.lite.test;

import com.xhtech.arch.ddd.annotation.EnableJwtAuth;
import com.xhtech.arch.ddd.annotation.EnableRestApi;
import com.xhtech.arch.ddd.annotation.EnableSecureApi;
import com.xhtech.arch.ddd.annotation.EnableSignApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@EnableRestApi
@EnableSignApi
@EnableJwtAuth
@EnableSecureApi
@ServletComponentScan
@SpringBootApplication
public class SpringLiteTestApplication
{
	public static void main(String[] args) {
		SpringApplication.run(SpringLiteTestApplication.class, args);
	}
}
