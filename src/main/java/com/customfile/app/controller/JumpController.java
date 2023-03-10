package com.customfile.app.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 跳控制器
 *
 * @author YCJ
 * @date 2023/03/07
 */
@Controller
public class JumpController {

    @RequestMapping
    public String toIndex() {
        return "index/index";
    }
}
