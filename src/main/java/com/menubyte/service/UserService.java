/**
 * Service for managing User entities.
 * Handles user creation, updates, authentication, and retrieval.
 *
 * @author Ankit Srivastava
 */
package com.menubyte.service;

import com.menubyte.entity.User;
import com.menubyte.exception.UserAlreadyExistsException;
import com.menubyte.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Create a new user (Signup).
     * @param user User entity.
     * @return Created User.
     */
    public User createUser(User user)  { // Removed 'throws' as it's a RuntimeException
        log.info("Creating user with email: {}", user.getEmail());
            // Check if a user with the same mobile number already exists
            Optional<User> existingUserByMobile = userRepository.findByMobileNumber(user.getMobileNumber());
            if (existingUserByMobile.isPresent()) {
                throw new UserAlreadyExistsException("A user with this mobile number already exists.");
            }

            // Check if a user with the same email already exists
            Optional<User> existingUserByEmail = userRepository.findByEmail(user.getEmail());
            if (existingUserByEmail.isPresent()) {
                throw new UserAlreadyExistsException("A user with this email already exists.");
            }

            // Only if no duplicates are found, save the new user and return the result
            return userRepository.save(user);
        }


    /**
     * Update an existing user.
     * @param id User ID.
     * @param updatedUser Updated user data.
     * @return Updated User.
     */
    public User updateUser(Long id, User updatedUser) {
        log.info("Updating user with ID: {}", id);
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPassword(updatedUser.getPassword());
            existingUser.setMobileNumber(updatedUser.getMobileNumber());
            return userRepository.save(existingUser);
        } else {
            log.error("User not found with ID: {}", id);
            throw new RuntimeException("User not found with ID: " + id);
        }
    }

    /**
     * Delete a user by ID.
     * @param id User ID.
     */
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }

    /**
     * Get a user by ID.
     * @param id User ID.
     * @return User entity.
     */
    public User getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new RuntimeException("User not found with ID: " + id);
                });
    }

    /**
     * Authenticate a user (Login with Mobile & Password).
     * @param mobileNumber Mobile number.
     * @param password Password.
     * @return Authenticated User.
     */
    public User authenticateUser(String mobileNumber, String password) {
        log.info("Authenticating user with mobile number: {}", mobileNumber);
        return userRepository.findByMobileNumberAndPassword(mobileNumber, password)
                .orElseThrow(() -> {
                    log.error("Invalid mobile number or password for user with mobile: {}", mobileNumber);
                    return new RuntimeException("Invalid mobile number or password");
                });
    }
}