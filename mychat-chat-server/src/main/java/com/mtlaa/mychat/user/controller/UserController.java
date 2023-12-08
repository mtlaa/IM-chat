package com.mtlaa.mychat.user.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author mtlaa
 * @since 2023-11-30
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @GetMapping()
    public String test(){
        log.info("get .......");
        return "jahaha";
    }
}

