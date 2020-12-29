package com.example.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.controllers.ItemController;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;

@RunWith(MockitoJUnitRunner.class)
public class ItemControllerTest {
    @InjectMocks
    private ItemController itemController;

    @Mock
    private ItemRepository repository;

    private static final String PRODUCT_1 = "product";

    private static final String PRODUCT_2 = "product2";

    private static final String PRODUCT_3 = "product3";

    @Before
    public void setup() {
        Item item1 = createItem(Long.valueOf(1), PRODUCT_1);
        Item item2 = createItem(Long.valueOf(2), PRODUCT_2);
        Item item3 = createItem(Long.valueOf(3), PRODUCT_3);
        when(repository.findByName(item2.getName())).thenReturn(Lists.list(item2, item3));
        when(repository.findById(1L)).thenReturn(Optional.of(item1));
        when(repository.findAll()).thenReturn(Lists.list(item1, item2, item3));
    }

    @Test
    public void findByItemId() {
        ResponseEntity<Item> responseEntity = itemController.getItemById(1L);
        Item item = responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());
        assertEquals(1L, item.getId().longValue());
    }

    @Test
    public void findAllItems() {
        ResponseEntity<List<Item>> responseEntity = itemController.getItems();
        List<Item> items = responseEntity.getBody();
        assertFalse(items.isEmpty());
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());
        assertEquals(3, items.size());
    }

    @Test
    public void findByNonExistentId() {
        ResponseEntity<Item> responseEntity = itemController.getItemById(3L);
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getStatusCode().value());
    }

    @Test
    public void findByNonExistentNAme() {
        ResponseEntity<List<Item>> responseEntity = itemController.getItemsByName("EmptyString");
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getStatusCode().value());
    }

    @Test
    public void findItemByName() {
        ResponseEntity<List<Item>> responseEntity = itemController.getItemsByName("product2");
        List<Item> items = responseEntity.getBody();
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());
        assertEquals(2, items.size());
    }

    private Item createItem(long id, String name) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        return item;
    }
}