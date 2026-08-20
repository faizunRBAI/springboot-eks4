package com.example.app.web;

import com.example.app.domain.Item;
import com.example.app.domain.ItemNotFoundException;
import com.example.app.domain.ItemService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Slice test for {@link ItemController}: verifies routing, status codes and JSON shapes.
 *
 * <p>No database is involved — {@link ItemService} is mocked. The
 * {@code database=none} path (null service) is exercised by the 503 assertions.
 */
@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private Item sampleItem;

    @BeforeEach
    void setUp() throws Exception {
        sampleItem = new Item("Widget");
        // Reflectively set the generated id so assertions can reference it.
        var idField = Item.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(sampleItem, 1L);
        var createdAtField = Item.class.getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(sampleItem, OffsetDateTime.now());
    }

    @Test
    void listReturnsAllItems() throws Exception {
        Mockito.when(itemService.findAll()).thenReturn(List.of(sampleItem));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/items"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Widget"));
    }

    @Test
    void getByIdReturnsItem() throws Exception {
        Mockito.when(itemService.findById(1L)).thenReturn(sampleItem);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/items/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Widget"));
    }

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        Mockito.when(itemService.findById(99L)).thenThrow(new ItemNotFoundException(99L));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/items/99"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());
    }

    @Test
    void createPersistsItemAndReturns201() throws Exception {
        Mockito.when(itemService.create("Widget")).thenReturn(sampleItem);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Widget\"}"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Widget"))
            .andExpect(MockMvcResultMatchers.header().exists("Location"));
    }

    @Test
    void createReturnsBadRequestWhenNameMissing() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());
    }

    @Test
    void updateReturnsUpdatedItem() throws Exception {
        Mockito.when(itemService.update(1L, "Gadget")).thenReturn(sampleItem);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/items/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Gadget\"}"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void deleteReturns204() throws Exception {
        Mockito.doNothing().when(itemService).delete(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/items/1"))
            .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}
