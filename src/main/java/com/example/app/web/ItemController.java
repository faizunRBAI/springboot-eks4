package com.example.app.web;

import com.example.app.domain.Item;
import com.example.app.domain.ItemNotFoundException;
import com.example.app.domain.ItemService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST API for the {@link Item} domain.
 *
 * <p>The controller is registered only when a datasource is present: the
 * {@code ItemService} bean depends on {@code ItemRepository}, which depends on
 * Spring Data JPA, which is auto-configured only when a datasource URL is set.
 * When no database is configured, requests to these endpoints return 503 via
 * the optional-injection guard below.
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService service;

    @Autowired
    public ItemController(@Autowired(required = false) ItemService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        if (service == null) {
            return noDatabaseResponse();
        }
        List<Item> items = service.findAll();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        if (service == null) {
            return noDatabaseResponse();
        }
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        if (service == null) {
            return noDatabaseResponse();
        }
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "'name' is required"));
        }
        Item created = service.create(name.trim());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        if (service == null) {
            return noDatabaseResponse();
        }
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "'name' is required"));
        }
        return ResponseEntity.ok(service.update(id, name.trim()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service == null) {
            return ResponseEntity.serviceUnavailable().build();
        }
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ItemNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    private ResponseEntity<Map<String, String>> noDatabaseResponse() {
        return ResponseEntity.status(503)
            .body(Map.of("error", "No database configured for this deployment."));
    }
}
