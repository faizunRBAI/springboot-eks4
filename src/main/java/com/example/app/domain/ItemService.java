package com.example.app.domain;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for the {@link Item} domain.
 *
 * <p>Wraps the repository to provide a transaction boundary and a clean API
 * for the REST layer. All public methods are read-only unless annotated otherwise.
 */
@Service
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    /** Returns all items, most recent first. */
    public List<Item> findAll() {
        return repository.findAllOrderedByCreatedAt();
    }

    /** Returns one item by id, or throws {@link ItemNotFoundException} when absent. */
    public Item findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
    }

    /** Creates and persists a new item. */
    @Transactional
    public Item create(String name) {
        return repository.save(new Item(name));
    }

    /** Updates the name of an existing item. */
    @Transactional
    public Item update(Long id, String name) {
        Item item = findById(id);
        item.setName(name);
        return repository.save(item);
    }

    /** Deletes an item. No-op when the id does not exist. */
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
