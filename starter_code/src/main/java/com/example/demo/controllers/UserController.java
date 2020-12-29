package com.example.demo.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		return ResponseEntity.of(userRepository.findById(id));
	}

	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		User user = userRepository.findByUsername(username).orElse(null);
		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}

	@PostMapping("/create")
	public ResponseEntity<?> createUser(@RequestBody CreateUserRequest createUserRequest) {

		LOGGER.info("CreateUserRequest {} ", createUserRequest);

		if(createUserRequest.getPassword().length() < 8){
			LOGGER.error("User not created because Password cannot be less than 8 characters");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password cannot be less than 8 characters");
		}

		if( !createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword())){
			LOGGER.error("User not created because Passwords are not the same");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords are not the same");
		}

		if (createUserRequest.getUsername() == null || createUserRequest.getUsername().isEmpty()) {
			LOGGER.info("Username {} cannot be null of empty", createUserRequest.getUsername());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		if( userRepository.findByUsername(createUserRequest.getUsername()).isPresent()){
			LOGGER.error("Username {} already exists", createUserRequest.getUsername());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}


		User user = new User();
		user.setUsername(createUserRequest.getUsername());
		user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));
		LOGGER.info("Encoded password is {}",user.getPassword());
		Cart cart = new Cart();
		cartRepository.save(cart);
		user.setCart(cart);
		User userSaved = userRepository.save(user);
		LOGGER.info("User Created {} ", userSaved);

		return ResponseEntity.ok(user);
	}

}
