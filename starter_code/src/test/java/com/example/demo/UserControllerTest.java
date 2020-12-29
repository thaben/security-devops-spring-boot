package com.example.demo;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    private static final String USERNAME = "johndoe123";
    private static final Long ID = 1L;
    private static final String PASSWORD = "p$assword";

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    private BCryptPasswordEncoder passwordEncoder = mock(BCryptPasswordEncoder.class);

    @Test
    public void findByUserId() {
        Optional<User> userOptional = createUser();
        userOptional.get().setId(ID);
        when(userRepository.findById(ID)).thenReturn(userOptional);
        ResponseEntity<User> response = userController.findById(ID);
        User user = response.getBody();
        Assert.assertEquals("The ids are not the same", ID.longValue(), user.getId());
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
    }

    @Test
    public void findByUsername() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(createUser());
        ResponseEntity<User> response = userController.findByUserName(USERNAME);
        User user = response.getBody();
        Assert.assertEquals( HttpStatus.OK.value(),response.getStatusCode().value());
        Assert.assertEquals( USERNAME,user.getUsername());
    }

    @Test
    public void findByNonExistentUsername() {
        when(userRepository.findByUsername("non existent username")).thenReturn(Optional.empty());
        ResponseEntity<User> response = userController.findByUserName("non existent username");
        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
    }

    @Test
    public void userCreation() {
        when(passwordEncoder.encode(anyString())).thenReturn(PASSWORD);
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(USERNAME);
        createUserRequest.setConfirmPassword(PASSWORD);
        createUserRequest.setPassword(PASSWORD);
        ResponseEntity<User> response = (ResponseEntity<User>) userController.createUser(createUserRequest);
        User createdUser = response.getBody();
        Assert.assertEquals(HttpStatus.OK.value(),response.getStatusCode().value() );
        Assert.assertEquals( PASSWORD,createdUser.getPassword());
        Assert.assertEquals(USERNAME,createdUser.getUsername());
    }

    @Test
    public void badPassword() {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(USERNAME);
        createUserRequest.setPassword(PASSWORD);
        ResponseEntity<User> response = (ResponseEntity<User>) userController.createUser(createUserRequest);
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(),response.getStatusCode().value() );
    }

    @Test
    public void nonMatchingPassword() {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(USERNAME);
        createUserRequest.setPassword("password");
        ResponseEntity<User> response = (ResponseEntity<User>) userController.createUser(createUserRequest);
        Assert.assertEquals(response.getStatusCodeValue(), HttpStatus.FORBIDDEN.value());
    }

    private Optional<User> createUser() {
        User user = new User();
        user.setUsername(USERNAME);
        return Optional.of(user);
    }
}