package ${package}.service;

import com.metaobjects.MetaObject;
import com.metaobjects.object.ValueMetaObject;
import com.metaobjects.manager.db.ObjectManagerDB;
import com.metaobjects.manager.ObjectConnection;
import com.metaobjects.util.MetaDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService demonstrating MetaObjects testing patterns.
 *
 * This test class shows:
 * - Mocking MetaObjects framework components
 * - Testing ValueMetaObject operations
 * - Verifying database interactions through ObjectManagerDB
 * - Testing exception handling and validation
 * - Mocking static utility methods
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private ObjectManagerDB objectManager;

    @Mock
    private ObjectConnection connection;

    @Mock
    private MetaObject userMetaObject;

    @Mock
    private ValueMetaObject mockUser;

    private UserService userService;

    @BeforeEach
    void setUp() throws Exception {
        // Mock the connection management
        when(objectManager.getConnection()).thenReturn(connection);

        // Mock MetaDataUtil.findMetaObjectByName to return our mock
        try (MockedStatic<MetaDataUtil> metaDataUtil = mockStatic(MetaDataUtil.class)) {
            metaDataUtil.when(() -> MetaDataUtil.findMetaObjectByName("User", any()))
                .thenReturn(userMetaObject);

            userService = new UserService(objectManager);
        }
    }

    @Test
    void testCreateUser_Success() throws Exception {
        // Arrange
        String username = "johndoe";
        String email = "john.doe@example.com";
        String firstName = "John";
        String lastName = "Doe";

        // Mock ValueMetaObject creation and behavior
        try (MockedStatic<ValueMetaObject> valueMetaObjectStatic = mockStatic(ValueMetaObject.class, CALLS_REAL_METHODS)) {
            ValueMetaObject userInstance = mock(ValueMetaObject.class);
            valueMetaObjectStatic.when(() -> new ValueMetaObject(userMetaObject))
                .thenReturn(userInstance);

            // Act
            ValueMetaObject result = userService.createUser(username, email, firstName, lastName);

            // Assert
            assertNotNull(result);

            // Verify field setters were called
            verify(userInstance).setString("username", username);
            verify(userInstance).setString("email", email);
            verify(userInstance).setString("firstName", firstName);
            verify(userInstance).setString("lastName", lastName);
            verify(userInstance).setBoolean("isActive", true);
            verify(userInstance).setTimestamp(eq("createdAt"), any(LocalDateTime.class));

            // Verify database operations
            verify(objectManager).createObject(connection, userInstance);
            verify(connection).close();
        }
    }

    @Test
    void testFindUserById_Success() throws Exception {
        // Arrange
        Long userId = 1L;

        try (MockedStatic<ValueMetaObject> valueMetaObjectStatic = mockStatic(ValueMetaObject.class, CALLS_REAL_METHODS)) {
            ValueMetaObject userInstance = mock(ValueMetaObject.class);
            valueMetaObjectStatic.when(() -> new ValueMetaObject(userMetaObject))
                .thenReturn(userInstance);

            when(objectManager.loadObject(connection, userInstance)).thenReturn(mockUser);

            // Act
            ValueMetaObject result = userService.findUserById(userId);

            // Assert
            assertNotNull(result);
            assertEquals(mockUser, result);

            // Verify the ID was set for lookup
            verify(userInstance).setLong("id", userId);
            verify(objectManager).loadObject(connection, userInstance);
            verify(connection).close();
        }
    }

    @Test
    void testFindUserByUsername_Success() throws Exception {
        // Arrange
        String username = "johndoe";

        try (MockedStatic<ValueMetaObject> valueMetaObjectStatic = mockStatic(ValueMetaObject.class, CALLS_REAL_METHODS)) {
            ValueMetaObject userInstance = mock(ValueMetaObject.class);
            valueMetaObjectStatic.when(() -> new ValueMetaObject(userMetaObject))
                .thenReturn(userInstance);

            when(objectManager.loadObject(connection, userInstance, "username_uk")).thenReturn(mockUser);

            // Act
            ValueMetaObject result = userService.findUserByUsername(username);

            // Assert
            assertNotNull(result);
            assertEquals(mockUser, result);

            // Verify the username was set for lookup
            verify(userInstance).setString("username", username);
            verify(objectManager).loadObject(connection, userInstance, "username_uk");
            verify(connection).close();
        }
    }

    @Test
    void testFindUserByEmail_Success() throws Exception {
        // Arrange
        String email = "john.doe@example.com";

        try (MockedStatic<ValueMetaObject> valueMetaObjectStatic = mockStatic(ValueMetaObject.class, CALLS_REAL_METHODS)) {
            ValueMetaObject userInstance = mock(ValueMetaObject.class);
            valueMetaObjectStatic.when(() -> new ValueMetaObject(userMetaObject))
                .thenReturn(userInstance);

            when(objectManager.loadObject(connection, userInstance, "email_uk")).thenReturn(mockUser);

            // Act
            ValueMetaObject result = userService.findUserByEmail(email);

            // Assert
            assertNotNull(result);
            assertEquals(mockUser, result);

            // Verify the email was set for lookup
            verify(userInstance).setString("email", email);
            verify(objectManager).loadObject(connection, userInstance, "email_uk");
            verify(connection).close();
        }
    }

    @Test
    void testFindActiveUsers_Success() throws Exception {
        // Arrange
        List<ValueMetaObject> expectedUsers = Arrays.asList(mockUser);
        when(objectManager.loadObjects(eq(connection), eq(userMetaObject),
                eq("is_active = ? ORDER BY created_at DESC"),
                argThat(params -> params.length == 1 && params[0].equals(true))))
            .thenReturn(expectedUsers);

        // Act
        List<ValueMetaObject> result = userService.findActiveUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockUser, result.get(0));
        verify(connection).close();
    }

    @Test
    void testFindAllUsers_Success() throws Exception {
        // Arrange
        List<ValueMetaObject> expectedUsers = Arrays.asList(mockUser);
        when(objectManager.loadObjects(eq(connection), eq(userMetaObject),
                eq("1=1 ORDER BY created_at DESC"),
                argThat(params -> params.length == 0)))
            .thenReturn(expectedUsers);

        // Act
        List<ValueMetaObject> result = userService.findAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockUser, result.get(0));
        verify(connection).close();
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // Arrange
        ValueMetaObject userToUpdate = mock(ValueMetaObject.class);

        // Act
        ValueMetaObject result = userService.updateUser(userToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(userToUpdate, result);

        // Verify database update was called
        verify(objectManager).updateObject(connection, userToUpdate);
        verify(connection).close();
    }

    @Test
    void testUpdateLastLogin_Success() throws Exception {
        // Arrange
        Long userId = 1L;

        // Mock findUserById method by mocking the service itself
        UserService spyUserService = spy(userService);
        doReturn(mockUser).when(spyUserService).findUserById(userId);

        // Act
        spyUserService.updateLastLogin(userId);

        // Assert
        verify(spyUserService).findUserById(userId);
        verify(mockUser).setTimestamp(eq("lastLoginAt"), any(LocalDateTime.class));
        verify(objectManager).updateObject(connection, mockUser);
    }

    @Test
    void testDeactivateUser_Success() throws Exception {
        // Arrange
        Long userId = 1L;

        // Mock findUserById method
        UserService spyUserService = spy(userService);
        doReturn(mockUser).when(spyUserService).findUserById(userId);

        // Act
        spyUserService.deactivateUser(userId);

        // Assert
        verify(spyUserService).findUserById(userId);
        verify(mockUser).setBoolean("isActive", false);
        verify(objectManager).updateObject(connection, mockUser);
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // Arrange
        Long userId = 1L;

        try (MockedStatic<ValueMetaObject> valueMetaObjectStatic = mockStatic(ValueMetaObject.class, CALLS_REAL_METHODS)) {
            ValueMetaObject userInstance = mock(ValueMetaObject.class);
            valueMetaObjectStatic.when(() -> new ValueMetaObject(userMetaObject))
                .thenReturn(userInstance);

            // Act
            userService.deleteUser(userId);

            // Assert
            verify(userInstance).setLong("id", userId);
            verify(objectManager).deleteObject(connection, userInstance);
            verify(connection).close();
        }
    }

    @Test
    void testSearchUsersByName_Success() throws Exception {
        // Arrange
        String searchTerm = "john";
        String searchPattern = "%" + searchTerm.toLowerCase() + "%";
        List<ValueMetaObject> expectedUsers = Arrays.asList(mockUser);

        when(objectManager.loadObjects(eq(connection), eq(userMetaObject),
                eq("(LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?) AND is_active = ? ORDER BY last_name, first_name"),
                argThat(params -> params.length == 3 &&
                                params[0].equals(searchPattern) &&
                                params[1].equals(searchPattern) &&
                                params[2].equals(true))))
            .thenReturn(expectedUsers);

        // Act
        List<ValueMetaObject> result = userService.searchUsersByName(searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockUser, result.get(0));
        verify(connection).close();
    }

    @Test
    void testCreateUser_DatabaseException() throws Exception {
        // Arrange
        String username = "johndoe";
        String email = "john.doe@example.com";
        String firstName = "John";
        String lastName = "Doe";

        try (MockedStatic<ValueMetaObject> valueMetaObjectStatic = mockStatic(ValueMetaObject.class, CALLS_REAL_METHODS)) {
            ValueMetaObject userInstance = mock(ValueMetaObject.class);
            valueMetaObjectStatic.when(() -> new ValueMetaObject(userMetaObject))
                .thenReturn(userInstance);

            // Simulate database exception
            doThrow(new RuntimeException("Database connection failed"))
                .when(objectManager).createObject(connection, userInstance);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.createUser(username, email, firstName, lastName);
            });

            assertEquals("Database connection failed", exception.getMessage());
            verify(connection).close(); // Ensure connection is still closed
        }
    }

    @Test
    void testFindUserById_NotFound() throws Exception {
        // Arrange
        Long userId = 999L;

        try (MockedStatic<ValueMetaObject> valueMetaObjectStatic = mockStatic(ValueMetaObject.class, CALLS_REAL_METHODS)) {
            ValueMetaObject userInstance = mock(ValueMetaObject.class);
            valueMetaObjectStatic.when(() -> new ValueMetaObject(userMetaObject))
                .thenReturn(userInstance);

            when(objectManager.loadObject(connection, userInstance)).thenReturn(null);

            // Act
            ValueMetaObject result = userService.findUserById(userId);

            // Assert
            assertNull(result);
            verify(connection).close();
        }
    }

    @Test
    void testConstructor_MetaDataUtilException() {
        // Arrange
        try (MockedStatic<MetaDataUtil> metaDataUtil = mockStatic(MetaDataUtil.class)) {
            metaDataUtil.when(() -> MetaDataUtil.findMetaObjectByName("User", any()))
                .thenThrow(new RuntimeException("MetaData not found"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                new UserService(objectManager);
            });

            assertEquals("MetaData not found", exception.getMessage());
        }
    }
}