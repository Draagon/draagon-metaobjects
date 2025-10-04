package ${package}.repository;

import ${package}.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository interface for User entity.
 *
 * This repository extends JpaRepository to provide:
 * - Standard CRUD operations (save, findById, findAll, delete, etc.)
 * - Custom query methods for business logic
 * - Type-safe query definitions
 * - Transaction management integration
 *
 * The User entity is generated from MetaObjects metadata,
 * ensuring consistency between domain model and data access layer.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username (unique constraint)
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email (unique constraint)
     */
    Optional<User> findByEmail(String email);

    /**
     * Find users by first name (case-insensitive)
     */
    List<User> findByFirstNameIgnoreCase(String firstName);

    /**
     * Find users by last name (case-insensitive)
     */
    List<User> findByLastNameIgnoreCase(String lastName);

    /**
     * Find users by first and last name
     */
    List<User> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Find users with email containing the given text
     */
    List<User> findByEmailContainingIgnoreCase(String emailPart);

    /**
     * Find users with username containing the given text
     */
    List<User> findByUsernameContainingIgnoreCase(String usernamePart);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find users by partial name match (first or last name)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContaining(@Param("name") String name);

    /**
     * Count users with given first name
     */
    long countByFirstName(String firstName);

    /**
     * Count users with given last name
     */
    long countByLastName(String lastName);
}