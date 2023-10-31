package controller;

import controllers.AuthenticationController;
import exceptions.IncorrectPassword;
import exceptions.NotExistentUser;
import exceptions.UsernameAlreadyTaken;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import service.Baloot;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AuthenticationControllerTest {
    @Mock
    private Baloot baloot;
    private AutoCloseable mockCloseable;
    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    public void setUp() {
        mockCloseable = MockitoAnnotations.openMocks(this);
        authenticationController.setBaloot(baloot);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void testLoginSuccess() throws NotExistentUser, IncorrectPassword {
        var username = "testUser";
        var password = "testPassword";
        doNothing().when(baloot).login(username, password);
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);

        ResponseEntity<String> response = authenticationController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("login successfully!", response.getBody());
    }

    @Test
    public void testLoginNotExistentUser() throws NotExistentUser, IncorrectPassword {
        var username = "nonExistentUser";
        var password = "testPassword";
        doThrow(NotExistentUser.class).when(baloot).login(username, password);
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);

        ResponseEntity<String> response = authenticationController.login(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testLoginIncorrectPassword() throws NotExistentUser, IncorrectPassword {
        var username = "testUser";
        var password = "incorrectPassword";
        doThrow(IncorrectPassword.class).when(baloot).login(username, password);
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);

        ResponseEntity<String> response = authenticationController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    public class UserMatcher implements ArgumentMatcher<User> {
        private final String address;
        private final String birthDate;
        private final String email;
        private final String username;
        private final String password;

        public UserMatcher(String address, String birthDate, String email, String username, String password) {
            this.address = address;
            this.birthDate = birthDate;
            this.email = email;
            this.username = username;
            this.password = password;
        }

        @Override
        public boolean matches(User user) {
            return user.getAddress().equals(address) &&
                    user.getBirthDate().equals(birthDate) &&
                    user.getEmail().equals(email) &&
                    user.getUsername().equals(username) &&
                    user.getPassword().equals(password);
        }
    }

    @Test
    public void testSignupSuccess() throws UsernameAlreadyTaken {
        var address = "testAddress";
        var birthDate = "birthDate";
        var email = "email";
        var username = "username";
        var password = "password";
        doNothing().when(baloot).addUser(argThat(new UserMatcher(address, birthDate, email, username, password)));
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        request.put("address", address);
        request.put("email", email);
        request.put("birthDate", birthDate);

        ResponseEntity<String> response = authenticationController.signup(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("signup successfully!", response.getBody());
    }

    @Test
    public void testSignupUsernameTaken() throws UsernameAlreadyTaken {
        var address = "testAddress";
        var birthDate = "birthDate";
        var email = "email";
        var username = "username";
        var password = "password";
        doThrow(UsernameAlreadyTaken.class).when(baloot).addUser(
                argThat(new UserMatcher(address, birthDate, email, username, password)
        ));
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        request.put("address", address);
        request.put("email", email);
        request.put("birthDate", birthDate);

        ResponseEntity<String> response = authenticationController.signup(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
