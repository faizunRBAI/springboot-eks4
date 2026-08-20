package com.example.app.domain;

import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
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

    private final ObjectProvider<ItemRepository> repositoryProvider;

    /**
     * The repository arrives through an {@link ObjectProvider} on purpose: it
     * exists only when a datasource is configured (Spring Data JPA is switched
     * off otherwise), and this application must boot without a database —
     * HealthEndpointsTest enforces exactly that. A plain constructor parameter
     * here makes the whole context refuse to start in the no-database case.
     */
    public ItemService(ObjectProvider<ItemRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    /** Whether a database (and therefore the repository) is configured. */
    public boolean isAvailable() {
        return repositoryProvider.getIfAvailable() != null;
    }

    private ItemRepository repository() {
        final ItemRepository repository = repositoryProvider.getIfAvailable();
        if (repository == null) {
            throw new IllegalStateException("No database is configured.");
        }
        return repository;
    }

    /** Returns all items, most recent first. */
    public List<Item> findAll() {
        return repository().findAllOrderedByCreatedAt();
    }

    /** Returns one item by id, or throws {@link ItemNotFoundException} when absent. */
    public Item findById(Long id) {
        return repository().findById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
    }

    /** Creates and persists a new item. */
    @Transactional
    public Item create(String name) {
        return repository().save(new Item(name));
    }

    /** Updates the name of an existing item. */
    @Transactional
    public Item update(Long id, String name) {
        Item item = findById(id);
        item.setName(name);
        return repository().save(item);
    }

    /** Deletes an item. No-op when the id does not exist. */
    @Transactional
    public void delete(Long id) {
        repository().deleteById(id);
    }
}
