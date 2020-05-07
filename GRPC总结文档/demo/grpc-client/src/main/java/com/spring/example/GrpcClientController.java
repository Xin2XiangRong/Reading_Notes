package com.spring.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by chaishuai on 2018/6/13.
 */

@RestController
public class GrpcClientController {
    @Autowired
    private  GrpcClientService grpcClientService;

    @RequestMapping("/")
    public String printServerData(@RequestParam(defaultValue = "ChaiShuai") String name) {
        return grpcClientService.sendServerData(name);
    }
}
