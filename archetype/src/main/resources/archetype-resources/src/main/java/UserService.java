package ${package}.service;

import ${package}.domain.User;
import ${package}.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for User entity operations using standard JPA patterns.
 *
 * This service demonstrates:
 * - Generated domain objects from MetaObjects metadata
 * - Standard Spring Data JPA repositories
 * - Clean separation between domain logic and persistence
 * - Proper transaction management
 * - Business validation and error handling
 *
 * The User domain object is generated from metadata:
 * - Abstract base class (AbstractUser) - regenerated from metadata
 * - Concrete class (User) - generated once, safe to modify
 * - Standard JPA annotations and validation
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new user
     */
    public User createUser(String username, String email, String firstName, String lastName) {
        // Validate required fields
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        // Create and save user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCreatedAt(LocalDateTime.now().toString());

        return userRepository.save(user);
    }

    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find all users
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Update user
     */
    public User updateUser(Long id, String email, String firstName, String lastName) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Check if email already exists for another user
        if (email != null && !email.equals(user.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new IllegalArgumentException("Email already exists: " + email);
            }
            user.setEmail(email);
        }

        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }

        user.setUpdatedAt(LocalDateTime.now().toString());

        return userRepository.save(user);
    }

    /**
     * Delete user by ID
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Check if user exists by username
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Check if user exists by email
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * Count total users
     */
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }
}