package com.example.demo;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.controllers.CartController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;

@RunWith(MockitoJUnitRunner.class)
public class ControllersTests {
    private static final String USERNAME = "superUsername";

    private static final String PRICE = "66";

    private static final Integer QUANTITY = 2;

    private static final Long ITEM_ID = 3L;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartController cartController;

    @Before
    public void setup() {
        Mockito.when(userRepository.findByUsername(USERNAME)).thenReturn(createUser());
        Mockito.when(itemRepository.findById(ITEM_ID)).thenReturn(createItem());
    }

    @Test
    public void newItemAddedCart() {
        ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
        modifyCartRequest.setUsername(USERNAME);
        modifyCartRequest.setItemId(ITEM_ID);
        modifyCartRequest.setQuantity(QUANTITY);

        ResponseEntity<Cart> response = cartController.addToCart(modifyCartRequest);
        Cart cart = response.getBody();
        assertEquals(3, cart.getItems().size());
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals(USERNAME, cart.getUser().getUsername());
        assertEquals(new BigDecimal(PRICE).multiply(BigDecimal.valueOf(3)), cart.getTotal());
    }

    @Test
    public void invalidITem() {
        ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
        modifyCartRequest.setUsername(USERNAME);
        modifyCartRequest.setItemId(0L);
        modifyCartRequest.setQuantity(QUANTITY);
        ResponseEntity<Cart> response = cartController.addToCart(modifyCartRequest);
        assertEquals(response.getStatusCodeValue(), HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void invalidUserName() {
        ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
        modifyCartRequest.setUsername("");
        modifyCartRequest.setItemId(ITEM_ID);
        modifyCartRequest.setQuantity(QUANTITY);
        ResponseEntity<Cart> response = cartController.addToCart(modifyCartRequest);
        assertEquals(response.getStatusCodeValue(), HttpStatus.NOT_FOUND.value());
    }


    @Test
    public void removeItemFromCart() {
        ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
        modifyCartRequest.setUsername(USERNAME);
        modifyCartRequest.setItemId(ITEM_ID);
        modifyCartRequest.setQuantity(1);

        ResponseEntity<Cart> response = cartController.removeFromCart(modifyCartRequest);
        Cart cart = response.getBody();

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals(USERNAME, cart.getUser().getUsername());
        assertEquals(0, cart.getItems().size());
        assertEquals(0, cart.getTotal().intValue());
    }

    @Test
    public void removeInvalidUsername() {
        ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
        modifyCartRequest.setUsername("UserName");
        modifyCartRequest.setItemId(ITEM_ID);
        modifyCartRequest.setQuantity(QUANTITY);
        ResponseEntity<Cart> response = cartController.removeFromCart(modifyCartRequest);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
    }

    @Test
    public void removeInvalidItemFromCart() {
        ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
        modifyCartRequest.setUsername(USERNAME);
        modifyCartRequest.setItemId(0L);
        modifyCartRequest.setQuantity(QUANTITY);
        ResponseEntity<Cart> response = cartController.removeFromCart(modifyCartRequest);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
    }

    private Optional<Item> createItem() {
        Item item = new Item();
        item.setId(ITEM_ID);
        item.setPrice(new BigDecimal(PRICE));
        return Optional.of(item);
    }

    private Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.addItem(createItem().get());
        return cart;
    }

    private Optional<User> createUser() {
        User user = new User();
        user.setUsername(USERNAME);
        user.setCart(createCart(user));
        return Optional.of(user);
    }



}

