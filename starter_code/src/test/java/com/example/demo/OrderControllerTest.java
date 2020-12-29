package com.example.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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

import com.example.demo.controllers.OrderController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;

@RunWith(MockitoJUnitRunner.class)
public class OrderControllerTest {

    private static final String USERNAME = "random_username";

    private static final String PRICE = "66.66";

    private static final Long ITEM_ID = 2L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderController orderController;

    @Before
    public void setup() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(createUser());
        when(orderRepository.findByUser(Mockito.any())).thenReturn(getUserOrders());
    }

    @Test
    public void orderSubmit() {
        ResponseEntity<UserOrder> response = orderController.submit(USERNAME);
        UserOrder userOrderList = response.getBody();
        assertEquals(HttpStatus.OK.value(),response.getStatusCode().value());
        assertEquals( USERNAME,userOrderList.getUser().getUsername());
        assertEquals( 1,userOrderList.getItems().size());
        assertEquals(userOrderList.getTotal(), new BigDecimal(PRICE));
    }

    @Test
    public void badUsername() {
        ResponseEntity<UserOrder> response = orderController.submit("nonExistentUsername");
        assertEquals(response.getStatusCodeValue(), HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void getOrderByUsername() {
        ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser(USERNAME);
        List<UserOrder> userOrderList = response.getBody();
        assertEquals( HttpStatus.OK.value(),response.getStatusCode().value());
        assertEquals( 1,userOrderList.size());
        UserOrder userOrder = userOrderList.get(0);
        assertEquals(USERNAME, userOrder.getUser().getUsername() );
        assertEquals(new BigDecimal(PRICE), userOrder.getTotal());
        assertFalse("Item list is empty", userOrder.getItems().isEmpty());
        assertEquals("Bad number of items", 1, userOrder.getItems().size());
    }

    @Test
    public void findByWrongUsername() {
        ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("NonExistentUsername");
        assertEquals(response.getStatusCodeValue(), HttpStatus.NOT_FOUND.value());
    }

    private Optional<Item> createItem() {
        Item item = new Item();
        item.setId(ITEM_ID);
        item.setPrice(new BigDecimal(PRICE));
        return Optional.of(item);
    }

    private List<UserOrder> getUserOrders() {
        UserOrder userOrder = UserOrder.createFromCart(createUser().get().getCart());
        return Arrays.asList(userOrder);
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