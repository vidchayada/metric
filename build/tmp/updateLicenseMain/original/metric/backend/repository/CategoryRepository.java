package metric.backend.repository;

import java.util.List;
import java.util.Optional;
import metric.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByVisibility(String visibility);
}