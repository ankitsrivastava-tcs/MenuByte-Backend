/**
 * Controller for managing Users.
 * Handles user authentication, retrieval, and updates.
 *
 * @author Ankit
 */
package com.menubyte.controller;

import com.menubyte.entity.User;
import com.menubyte.exception.UserAlreadyExistsException;
import com.menubyte.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * User Signup.
     * @param user The user details.
     * @return The created User object.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> userSignup(@RequestBody User user) {

            User createdUser = userService.createUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);

        }

    /**
     * Update User details.
     * @param id The user ID.
     * @param updatedUser The updated user details.
     * @return The updated User object.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(user);
    }

    /**
     * Delete a User.
     * @param id The user ID.
     * @return Success message.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    /**
     * Get User by ID.
     * @param id The user ID.
     * @return The retrieved User object.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * User Login using mobile number and password.
     * @param mobileNumber The user's mobile number.
     * @param password The user's password.
     * @return JSON response with user details or an error message.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String mobileNumber, @RequestParam String password) {
        User user = userService.authenticateUser(mobileNumber, password);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid mobile number or password");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("mobileNumber", user.getMobileNumber());

        return ResponseEntity.ok(response);
    }
}
