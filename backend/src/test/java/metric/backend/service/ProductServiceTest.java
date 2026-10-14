package metric.backend.service;

import metric.backend.dto.*;
import metric.backend.entity.*;
import metric.backend.mapper.ProductMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock ProductRepository productRepository;
  @Mock CategoryRepository categoryRepository;
  @Mock ProductMapper productMapper;

  @InjectMocks ProductService service;

  private Category category(Long id, String name) {
    return Category.builder()
        .categoryId(id).categoryName(name).slug(name.toLowerCase())
        .visibility("show").build();
  }

  private Product product(Long id, Category category, String name, String desc) {
    return Product.builder().productId(id).category(category)
        .productName(name).description(desc)
        .basePrice(new BigDecimal("100")).build();
  }

  @Test
  void getAllCategories_success() {
    Category c = category(1L, "Clothing");
    when(categoryRepository.findByVisibility("show")).thenReturn(List.of(c));
    when(productMapper.toCategoryResponse(c))
        .thenReturn(CategoryResponse.builder().categoryId(1L).build());

    assertEquals(1, service.getAllCategories().size());
  }

  @Test
  void getAllCategories_empty() {
    when(categoryRepository.findByVisibility("show")).thenReturn(List.of());

    assertTrue(service.getAllCategories().isEmpty());
  }

  @Test
  void getAllProducts_success() {
    Product p = product(1L, null, "Shirt", "Cotton");
    when(productRepository.findAll()).thenReturn(List.of(p));
    when(productMapper.toProductResponse(p))
        .thenReturn(ProductResponse.builder().productId(1L).build());

    assertEquals(1, service.getAllProducts().size());
  }

  @Test
  void getProductById_found() {
    Product p = product(1L, null, "Shirt", "Cotton");
    when(productRepository.findById(1L)).thenReturn(Optional.of(p));
    when(productMapper.toProductResponse(p))
        .thenReturn(ProductResponse.builder().productId(1L).build());

    assertNotNull(service.getProductById(1L));
  }

  @Test
  void getProductById_missing() {
    when(productRepository.findById(1L)).thenReturn(Optional.empty());

    assertNull(service.getProductById(1L));
  }

  @Test
  void getProductsByCategorySlug_success() {
    Product p = product(1L, null, "Shirt", "Cotton");
    when(productRepository.findByCategory_Slug("clothing"))
        .thenReturn(List.of(p));
    when(productMapper.toProductResponse(p))
        .thenReturn(ProductResponse.builder().productId(1L).build());

    assertEquals(1, service.getProductsByCategorySlug("clothing").size());
  }

  @Test
  void searchProducts_nullReturnsAll() {
    Product p = product(1L, null, "Shirt", "Cotton");
    when(productRepository.findAll()).thenReturn(List.of(p));
    when(productMapper.toProductResponse(p))
        .thenReturn(ProductResponse.builder().productId(1L).build());

    assertEquals(1, service.searchProducts(null).size());
  }

  @Test
  void searchProducts_blankReturnsAll() {
    when(productRepository.findAll()).thenReturn(List.of());

    assertTrue(service.searchProducts("  ").isEmpty());
  }

  @Test
  void searchProducts_keywordIsTrimmed() {
    Product p = product(1L, null, "Shirt", "Cotton");
    when(productRepository.searchProducts("shirt")).thenReturn(List.of(p));
    when(productMapper.toProductResponse(p))
        .thenReturn(ProductResponse.builder().productId(1L).build());

    assertEquals(1, service.searchProducts(" shirt ").size());
  }

  @Test
  void getProducts_filtersByCategoryAndName() {
    Category c1 = category(1L, "Clothing");
    Category c2 = category(2L, "Shoes");

    Product match = product(1L, c1, "Black Shirt", "Cotton");
    Product wrongCategory = product(2L, c2, "Black Shoes", "Leather");
    Product wrongName = product(3L, c1, "White Shirt", "Cotton");

    when(productRepository.findAll())
        .thenReturn(List.of(match, wrongCategory, wrongName));
    when(productMapper.toProductResponse(match))
        .thenReturn(ProductResponse.builder().productId(1L).build());

    List<ProductResponse> result = service.getProducts(1L, " black ");

    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getProductId());
  }

  @Test
  void getProducts_searchMatchesDescription() {
    Product p = product(1L, null, "Item", "Special cotton");
    when(productRepository.findAll()).thenReturn(List.of(p));
    when(productMapper.toProductResponse(p))
        .thenReturn(ProductResponse.builder().productId(1L).build());

    assertEquals(1, service.getProducts(null, "COTTON").size());
  }

  @Test
  void getProducts_categoryFilterAllowsProductsWithoutCategory() {
    Product p = product(1L, null, "Item", "Description");
    when(productRepository.findAll()).thenReturn(List.of(p));
    when(productMapper.toProductResponse(p))
        .thenReturn(ProductResponse.builder().productId(1L).build());

    assertEquals(1, service.getProducts(99L, null).size());
  }

  @Test
  void getProducts_nullNameAndDescriptionDoesNotMatchSearch() {
    Product p = product(1L, null, null, null);
    when(productRepository.findAll()).thenReturn(List.of(p));

    assertTrue(service.getProducts(null, "shirt").isEmpty());
  }
}
