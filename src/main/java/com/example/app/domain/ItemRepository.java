package com.example.app.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Item}.
 *
 * <p>Spring Boot auto-configures this when {@code SPRING_DATASOURCE_URL} is present.
 * The repository bean is absent when no datasource is configured, which is safe because
 * the controller guards against that case.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /** Returns items ordered by creation time, most recent first. */
    @Query("SELECT i FROM Item i ORDER BY i.createdAt DESC")
    List<Item> findAllOrderedByCreatedAt();
}
