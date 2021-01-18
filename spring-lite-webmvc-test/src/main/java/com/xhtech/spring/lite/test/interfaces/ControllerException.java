package com.xhtech.spring.lite.test.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ControllerException
{
    private static final Logger logger = LoggerFactory.getLogger(ControllerException.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> whenClientInputIllegal(IllegalArgumentException ex, HttpServletRequest request)
    {
        logger.info("客户端错误 [method={}\turl={}\tquery={}], message: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                ex.getMessage(),
                ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> whenServerFails(Exception ex, HttpServletRequest request)
    {
        logger.error("服务端错误 [method={}\turl={}\tquery={}], message: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                ex.getMessage(),
                ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}