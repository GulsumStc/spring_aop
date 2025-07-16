package com.gs.spring_aop;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

/**
 * REST Controller for User operations
 * AOP will automatically log all HTTP requests and responses
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Create a new user
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        logger.info("Received request to create user: {}", user.getEmail());

        try {
            User createdUser = userService.createUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get all users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Received request to get all users");

        try {
            List<User> users = userService.getAllUsers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error getting users: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("Received request to get user by ID: {}", id);

        try {
            User user = userService.getUserById(id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error getting user by ID {}: {}", id, e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Update user
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        logger.info("Received request to update user with ID: {}", id);

        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating user with ID {}: {}", id, e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete user
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        logger.info("Received request to delete user with ID: {}", id);

        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(
                    Map.of("message", "User deleted successfully"),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            logger.error("Error deleting user with ID {}: {}", id, e.getMessage());
            return new ResponseEntity<>(
                    Map.of("error", "User not found"),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    /**
     * Find user by email
     * GET /api/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> findByEmail(@PathVariable String email) {
        logger.info("Received request to find user by email: {}", email);

        try {
            User user = userService.findByEmail(email);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error finding user by email {}: {}", email, e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Search users by name
     * GET /api/users/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam String name) {
        logger.info("Received request to search users by name: {}", name);

        try {
            List<User> users = userService.searchUsersByName(name);
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error searching users by name {}: {}", name, e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Authenticate user
     * POST /api/users/authenticate
     */
    @PostMapping("/authenticate")
    public ResponseEntity<Map<String, Object>> authenticateUser(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        logger.info("Received authentication request for email: {}", email);

        try {
            boolean isAuthenticated = userService.authenticateUser(email, password);

            if (isAuthenticated) {
                return new ResponseEntity<>(
                        Map.of("authenticated", true, "message", "Authentication successful"),
                        HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                        Map.of("authenticated", false, "message", "Invalid credentials"),
                        HttpStatus.UNAUTHORIZED
                );
            }
        } catch (Exception e) {
            logger.error("Error during authentication: {}", e.getMessage());
            return new ResponseEntity<>(
                    Map.of("authenticated", false, "error", "Authentication failed"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get user statistics
     * GET /api/users/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        logger.info("Received request to get user statistics");

        try {
            long totalUsers = userService.getUserCount();

            Map<String, Object> stats = Map.of(
                    "totalUsers", totalUsers,
                    "timestamp", java.time.LocalDateTime.now()
            );

            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error getting user statistics: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Health check endpoint
     * GET /api/users/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return new ResponseEntity<>(
                Map.of("status", "OK", "service", "User Service"),
                HttpStatus.OK
        );
    }
}
