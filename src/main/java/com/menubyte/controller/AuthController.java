//package com.menubyte.controller;
//
//
//import com.menubyte.entity.User;
//import com.menubyte.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/auth")
//@CrossOrigin(origins = "http://localhost:3000")  // Allow only your React app to access this endpoint
//public class AuthController {
//
//    @Autowired
//    private UserService userService;
//
//    @PostMapping("/signup")
//    public User register(@RequestBody User user) {
//        return userService.registerUser(user);
//    }
//}
