package com.gs.spring_aop;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new user
     * AOP will log this method call and monitor performance
     */
    public User createUser(User user) {
        logger.info("Creating user with email: {}", user.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with email " + user.getEmail() + " already exists");
        }

        // Set timestamps
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());

        // Simulate some processing time
        try {
            Thread.sleep(100); // 100ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Get user by ID with caching
     * AOP will cache the result for 30 seconds
     */

    public User getUserById(Long id) {
        logger.info("Fetching user with ID: {}", id);

        // Simulate database delay
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + id);
        }

        return user.get();
    }

    /**
     * Get all users
     * AOP will log this method call
     */

    public List<User> getAllUsers() {
        logger.info("Fetching all users");

        // Simulate processing time
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<User> users = userRepository.findAll();
        logger.info("Found {} users", users.size());
        return users;
    }

    /**
     * Update user
     * AOP will log this method call and monitor performance
     */

    public User updateUser(Long id, User userDetails) {
        logger.info("Updating user with ID: {}", id);

        User user = getUserById(id); // This will use cache if available

        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPassword() != null) {
            user.setPassword(userDetails.getPassword());
        }

        user.setUpdatedAt(java.time.LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully");
        return updatedUser;
    }

    /**
     * Delete user
     * AOP will log this method call and apply security monitoring
     */

    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);

        User user = getUserById(id);
        userRepository.delete(user);

        logger.info("User deleted successfully");
    }

    /**
     * Find user by email
     * AOP will cache the result
     */

    public User findByEmail(String email) {
        logger.info("Finding user by email: {}", email);

        // Simulate processing time
        try {
            Thread.sleep(75);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        return user.get();
    }

    /**
     * Search users by name
     * AOP will log this method call
     */

    public List<User> searchUsersByName(String name) {
        logger.info("Searching users by name: {}", name);

        // Simulate processing time
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<User> users = userRepository.findByNameContainingIgnoreCase(name);
        logger.info("Found {} users matching name: {}", users.size(), name);
        return users;
    }

    /**
     * Authenticate user
     * AOP will log this method call and apply security monitoring
     */

    public boolean authenticateUser(String email, String password) {
        logger.info("Authenticating user with email: {}", email);

        // Simulate authentication processing
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Optional<User> user = userRepository.findByEmailAndPassword(email, password);
        boolean isAuthenticated = user.isPresent();

        if (isAuthenticated) {
            logger.info("User authenticated successfully");
        } else {
            logger.warn("Authentication failed for email: {}", email);
        }

        return isAuthenticated;
    }

    /**
     * Get user count
     * AOP will cache this result
     */

    public long getUserCount() {
        logger.info("Getting total user count");

        // Simulate processing time
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long count = userRepository.count();
        logger.info("Total user count: {}", count);
        return count;
    }
}