package metric.backend.service;

import jakarta.persistence.EntityManager;
import metric.backend.dto.*;
import metric.backend.entity.*;
import metric.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

  @Mock ProductRepository productRepository;
  @Mock CategoryRepository categoryRepository;
  @Mock ProductImageRepository productImageRepository;
  @Mock ProductAttributeRepository productAttributeRepository;
  @Mock ProductService productService;
  @Mock EntityManager entityManager;

  @InjectMocks AdminProductService service;

  private Category category;
  private Product product;
  private CreateProductRequest request;

  @BeforeEach
  void setUp() {
    category = Category.builder()
        .categoryId(1L)
        .categoryName("Clothing")
        .slug("clothing")
        .build();

    product = Product.builder()
        .productId(10L)
        .productName("Shirt")
        .basePrice(new BigDecimal("100.00"))
        .category(category)
        .build();

    request = CreateProductRequest.builder()
        .productName("Shirt")
        .description("Cotton shirt")
        .basePrice(new BigDecimal("100.00"))
        .categoryId(1L)
        .imageUrls(List.of("/shirt.jpg"))
        .attributes(List.of(
            CreateProductAttributeRequest.builder()
                .size("M")
                .color("Black")
                .stockQty(10)
                .build()))
        .build();

    ReflectionTestUtils.setField(service, "entityManager", entityManager);
  }

  @Test
  void createProduct_success() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productService.getProductById(10L))
        .thenReturn(ProductResponse.builder().productId(10L).build());

    ProductResponse result = service.createProduct(request, null);

    assertEquals(10L, result.getProductId());
    verify(productImageRepository).saveAll(argThat(list ->
        ((List<?>) list).size() == 1));
    verify(productAttributeRepository).saveAll(argThat(list ->
        ((List<?>) list).size() == 1));
    verify(entityManager).flush();
    verify(entityManager).clear();
  }

  @Test
  void createProduct_withoutImagesAndAttributes() {
    request.setImageUrls(null);
    request.setAttributes(null);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productService.getProductById(10L)).thenReturn(
        ProductResponse.builder().productId(10L).build());

    assertNotNull(service.createProduct(request, null));

    verify(productImageRepository, never()).saveAll(anyList());
    verify(productAttributeRepository, never()).saveAll(anyList());
  }

  @Test
  void createProduct_categoryNotFound() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.createProduct(request, null));

    verify(productRepository, never()).save(any());
  }

  @Test
  void createProduct_withEmptyFileList() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productService.getProductById(10L)).thenReturn(
        ProductResponse.builder().productId(10L).build());

    assertNotNull(service.createProduct(request, List.of()));
  }

  @Test
  void createProduct_withUploadedFile() {
    request.setImageUrls(null);

    MockMultipartFile file = new MockMultipartFile(
        "images", "shirt.png", "image/png", "image-data".getBytes());

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productService.getProductById(10L)).thenReturn(
        ProductResponse.builder().productId(10L).build());

    assertNotNull(service.createProduct(request, List.of(file)));

    verify(productImageRepository).saveAll(anyList());
  }

  @Test
  void createProduct_uploadedFileWithoutExtension() {
    request.setImageUrls(null);

    MockMultipartFile file = new MockMultipartFile(
        "images", "shirt", "image/jpeg", "image-data".getBytes());

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productService.getProductById(10L)).thenReturn(
        ProductResponse.builder().productId(10L).build());

    assertNotNull(service.createProduct(request, List.of(file)));
  }

  @Test
  void createProduct_skipsEmptyUploadedFile() {
    request.setImageUrls(null);

    MockMultipartFile file = new MockMultipartFile(
        "images", "empty.jpg", "image/jpeg", new byte[0]);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productService.getProductById(10L)).thenReturn(
        ProductResponse.builder().productId(10L).build());

    assertNotNull(service.createProduct(request, List.of(file)));

    verify(productImageRepository, never()).saveAll(anyList());
  }

  @Test
  void updateProduct_success() {
    when(productRepository.findById(10L)).thenReturn(Optional.of(product));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productService.getProductById(10L)).thenReturn(
        ProductResponse.builder().productId(10L).build());

    assertNotNull(service.updateProduct(10L, request, null));

    verify(productImageRepository).deleteByProduct_ProductId(10L);
    verify(productAttributeRepository).deleteByProduct_ProductId(10L);
    verify(entityManager).flush();
    verify(entityManager).clear();
  }

  @Test
  void updateProduct_withoutReplacingImages() {
    request.setImageUrls(null);
    request.setAttributes(null);

    when(productRepository.findById(10L)).thenReturn(Optional.of(product));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productService.getProductById(10L)).thenReturn(
        ProductResponse.builder().productId(10L).build());

    assertNotNull(service.updateProduct(10L, request, null));

    verify(productImageRepository, never())
        .deleteByProduct_ProductId(anyLong());
    verify(productAttributeRepository, never())
        .deleteByProduct_ProductId(anyLong());
  }

  @Test
  void updateProduct_productNotFound() {
    when(productRepository.findById(10L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.updateProduct(10L, request, null));
  }

  @Test
  void updateProduct_categoryNotFound() {
    when(productRepository.findById(10L)).thenReturn(Optional.of(product));
    when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.updateProduct(10L, request, null));
  }

  @Test
  void updateProduct_emptyAttributesDeletesOldAttributes() {
    request.setAttributes(List.of());

    when(productRepository.findById(10L))
        .thenReturn(Optional.of(product));
    when(categoryRepository.findById(1L))
        .thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class)))
        .thenReturn(product);
    when(productService.getProductById(10L))
        .thenReturn(ProductResponse.builder()
            .productId(10L)
            .build());

    ProductResponse response =
        service.updateProduct(10L, request, null);

    assertNotNull(response);

    verify(productAttributeRepository)
        .deleteByProduct_ProductId(10L);

    verify(productAttributeRepository, never())
        .saveAll(anyList());
  }

  @Test
  void deleteProduct_success() {
    when(productRepository.existsById(10L)).thenReturn(true);

    service.deleteProduct(10L);

    verify(productAttributeRepository).deleteByProduct_ProductId(10L);
    verify(productImageRepository).deleteByProduct_ProductId(10L);
    verify(productRepository).deleteById(10L);
  }

  @Test
  void deleteProduct_notFound() {
    when(productRepository.existsById(10L)).thenReturn(false);

    assertThrows(IllegalArgumentException.class,
        () -> service.deleteProduct(10L));

    verify(productRepository, never()).deleteById(anyLong());
  }
}
