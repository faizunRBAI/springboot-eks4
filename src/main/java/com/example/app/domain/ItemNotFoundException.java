package com.example.app.domain;

/**
 * Thrown when an {@link Item} with the requested id does not exist.
 * The REST layer maps this to a 404 response.
 */
public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long id) {
        super("Item not found: " + id);
    }
}
