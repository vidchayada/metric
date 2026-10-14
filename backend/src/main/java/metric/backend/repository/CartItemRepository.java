/*
 * Copyright (c) 2026 Vidchayada
 * Licensed under the MIT License.
 */

package metric.backend.repository;

import java.util.List;
import java.util.Optional;
import metric.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser_UserId(Long userId);
    Optional<CartItem> findByUser_UserIdAndProduct_ProductIdAndAttribute_AttributeId(
        Long userId, Long productId, Long attributeId);
    void deleteByUser_UserId(Long userId);
}