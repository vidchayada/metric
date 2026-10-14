package metric.backend.service;

import metric.backend.dto.*;
import metric.backend.entity.*;
import metric.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @Mock CartItemRepository cartItemRepository;
  @Mock ProductRepository productRepository;
  @Mock ProductAttributeRepository productAttributeRepository;
  @Mock UserRepository userRepository;

  @InjectMocks CartService service;

  private User user() {
    return User.builder().userId(1L).username("alice").build();
  }

  private Product product() {
    return Product.builder()
        .productId(2L)
        .productName("Shirt")
        .basePrice(new BigDecimal("100"))
        .images(List.of(
            ProductImage.builder()
                .imageUrl("/first.jpg")
                .isPrimary(false).build(),
            ProductImage.builder()
                .imageUrl("/primary.jpg")
                .isPrimary(true).build()))
        .build();
  }

  private ProductAttribute attribute(int stock) {
    return ProductAttribute.builder()
        .attributeId(3L).size("M").color("Black")
        .stockQty(stock).build();
  }

  private CartItem item(int quantity) {
    return CartItem.builder()
        .cartItemId(4L).user(user()).product(product())
        .attribute(attribute(20)).quantity(quantity).build();
  }

  @Test
  void getCart_empty() {
    when(cartItemRepository.findByUser_UserId(1L)).thenReturn(List.of());

    CartResponse response = service.getCart(1L);

    assertTrue(response.getItems().isEmpty());
    assertEquals(0, response.getTotalItems());
    assertEquals(BigDecimal.ZERO, response.getGrandTotal());
  }

  @Test
  void getCart_calculatesTotalsAndPrimaryImage() {
    when(cartItemRepository.findByUser_UserId(1L))
        .thenReturn(List.of(item(2)));

    CartResponse response = service.getCart(1L);

    assertEquals(2, response.getTotalItems());
    assertEquals(new BigDecimal("200"), response.getGrandTotal());
    assertEquals("/primary.jpg", response.getItems().get(0).getImageUrl());
  }

  @Test
  void getCart_usesFirstImageWhenNoPrimary() {
    Product p = product();
    p.setImages(List.of(
        ProductImage.builder().imageUrl("/first.jpg")
            .isPrimary(false).build()));

    when(cartItemRepository.findByUser_UserId(1L))
        .thenReturn(List.of(CartItem.builder()
            .cartItemId(4L).user(user()).product(p)
            .attribute(attribute(10)).quantity(1).build()));

    assertEquals("/first.jpg",
        service.getCart(1L).getItems().get(0).getImageUrl());
  }

  @Test
  void getCart_withoutImages() {
    Product p = product();
    p.setImages(null);

    when(cartItemRepository.findByUser_UserId(1L))
        .thenReturn(List.of(CartItem.builder()
            .user(user()).product(p).attribute(attribute(10))
            .quantity(1).build()));

    assertNull(service.getCart(1L).getItems().get(0).getImageUrl());
  }

  @Test
  void addToCart_newItem() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
    when(productRepository.findById(2L)).thenReturn(Optional.of(product()));
    when(productAttributeRepository.findById(3L))
        .thenReturn(Optional.of(attribute(10)));
    when(cartItemRepository
        .findByUser_UserIdAndProduct_ProductIdAndAttribute_AttributeId(1L, 2L, 3L))
        .thenReturn(Optional.empty());
    when(cartItemRepository.findByUser_UserId(1L)).thenReturn(List.of());

    CartResponse response = service.addToCart(1L,
        new AddToCartRequest(2L, 3L, 2));

    assertEquals(0, response.getTotalItems());
    verify(cartItemRepository).save(any(CartItem.class));
  }

  @Test
  void addToCart_incrementsExistingItem() {
    CartItem existing = item(2);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
    when(productRepository.findById(2L)).thenReturn(Optional.of(product()));
    when(productAttributeRepository.findById(3L))
        .thenReturn(Optional.of(attribute(10)));
    when(cartItemRepository
        .findByUser_UserIdAndProduct_ProductIdAndAttribute_AttributeId(1L, 2L, 3L))
        .thenReturn(Optional.of(existing));
    when(cartItemRepository.findByUser_UserId(1L)).thenReturn(List.of());

    service.addToCart(1L, new AddToCartRequest(2L, 3L, 3));

    assertEquals(5, existing.getQuantity());
  }

  @Test
  void addToCart_userNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.addToCart(1L, new AddToCartRequest(2L, 3L, 1)));
  }

  @Test
  void addToCart_productNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
    when(productRepository.findById(2L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.addToCart(1L, new AddToCartRequest(2L, 3L, 1)));
  }

  @Test
  void addToCart_attributeNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
    when(productRepository.findById(2L)).thenReturn(Optional.of(product()));
    when(productAttributeRepository.findById(3L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.addToCart(1L, new AddToCartRequest(2L, 3L, 1)));
  }

  @Test
  void addToCart_quantityExceedsStock() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
    when(productRepository.findById(2L)).thenReturn(Optional.of(product()));
    when(productAttributeRepository.findById(3L))
        .thenReturn(Optional.of(attribute(1)));

    assertThrows(IllegalArgumentException.class,
        () -> service.addToCart(1L, new AddToCartRequest(2L, 3L, 2)));
  }

  @Test
  void addToCart_existingQuantityExceedsStock() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
    when(productRepository.findById(2L)).thenReturn(Optional.of(product()));
    when(productAttributeRepository.findById(3L))
        .thenReturn(Optional.of(attribute(3)));
    when(cartItemRepository
        .findByUser_UserIdAndProduct_ProductIdAndAttribute_AttributeId(1L, 2L, 3L))
        .thenReturn(Optional.of(item(2)));

    assertThrows(IllegalArgumentException.class,
        () -> service.addToCart(1L, new AddToCartRequest(2L, 3L, 2)));
  }

  @Test
  void updateCartItem_success() {
    CartItem existing = item(1);
    when(cartItemRepository.findById(4L)).thenReturn(Optional.of(existing));
    when(cartItemRepository.findByUser_UserId(1L)).thenReturn(List.of());

    service.updateCartItem(1L, 4L, new UpdateCartItemRequest(3));

    assertEquals(3, existing.getQuantity());
    verify(cartItemRepository).save(existing);
  }

  @Test
  void updateCartItem_notFound() {
    when(cartItemRepository.findById(4L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.updateCartItem(1L, 4L, new UpdateCartItemRequest(1)));
  }

  @Test
  void updateCartItem_wrongOwner() {
    CartItem existing = item(1);
    existing.getUser().setUserId(99L);
    when(cartItemRepository.findById(4L)).thenReturn(Optional.of(existing));

    assertThrows(IllegalArgumentException.class,
        () -> service.updateCartItem(1L, 4L, new UpdateCartItemRequest(1)));
  }

  @Test
  void updateCartItem_quantityExceedsStock() {
    CartItem existing = item(1);
    existing.setAttribute(attribute(1));
    when(cartItemRepository.findById(4L)).thenReturn(Optional.of(existing));

    assertThrows(IllegalArgumentException.class,
        () -> service.updateCartItem(1L, 4L, new UpdateCartItemRequest(2)));
  }

  @Test
  void removeCartItem_success() {
    CartItem existing = item(1);
    when(cartItemRepository.findById(4L)).thenReturn(Optional.of(existing));
    when(cartItemRepository.findByUser_UserId(1L)).thenReturn(List.of());

    service.removeCartItem(1L, 4L);

    verify(cartItemRepository).delete(existing);
  }

  @Test
  void removeCartItem_notFound() {
    when(cartItemRepository.findById(4L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.removeCartItem(1L, 4L));
  }

  @Test
  void removeCartItem_wrongOwner() {
    CartItem existing = item(1);
    existing.getUser().setUserId(99L);
    when(cartItemRepository.findById(4L)).thenReturn(Optional.of(existing));

    assertThrows(IllegalArgumentException.class,
        () -> service.removeCartItem(1L, 4L));
  }

  @Test
  void clearCart_deletesByUser() {
    service.clearCart(1L);
    verify(cartItemRepository).deleteByUser_UserId(1L);
  }
}
