package metric.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import metric.backend.dto.CategoryResponse;
import metric.backend.dto.ProductResponse;
import metric.backend.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  @Mock ProductService productService;

  @InjectMocks ProductController controller;

  @Test
  void getCategoriesShouldReturnFormattedCategories() {
    CategoryResponse category = mock(CategoryResponse.class);
    when(category.getCategoryId()).thenReturn(1L);
    when(category.getCategoryName()).thenReturn("Shoes");
    when(category.getSlug()).thenReturn("shoes");
    when(productService.getAllCategories()).thenReturn(List.of(category));

    var response = controller.getCategories();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
    assertEquals("Shoes", response.getBody().get(0).get("name"));
    assertNull(response.getBody().get(0).get("image"));
  }

  @Test
  void getCategoriesShouldHandleNullCategories() {
    when(productService.getAllCategories()).thenReturn(null);

    var response = controller.getCategories();

    assertTrue(response.getBody().isEmpty());
  }

  @Test
  void getProductsShouldUseCategorySlug() {
    List<ProductResponse> products = List.of(mock(ProductResponse.class));
    when(productService.getProductsByCategorySlug("shoes"))
        .thenReturn(products);

    var response = controller.getProducts(
        " shoes ", null, null, null, 1, 100);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().get("totalItems"));
    verify(productService).getProductsByCategorySlug("shoes");
    verify(productService, never()).getAllProducts();
  }

  @Test
  void getProductsShouldPrioritizeQOverSearchAndKeyword() {
    List<ProductResponse> products = List.of(mock(ProductResponse.class));
    when(productService.getProducts(null, "first"))
        .thenReturn(products);

    var response = controller.getProducts(
        null, "second", " first ", "third", 1, 100);

    assertEquals(1, response.getBody().get("totalItems"));
    verify(productService).getProducts(null, "first");
  }

  @Test
  void getProductsShouldUseSearchWhenQIsBlank() {
    when(productService.getProducts(null, "shoe"))
        .thenReturn(List.of());

    controller.getProducts(null, " shoe ", " ", null, 1, 100);

    verify(productService).getProducts(null, "shoe");
  }

  @Test
  void getProductsShouldUseKeywordAsFallback() {
    when(productService.getProducts(null, "book"))
        .thenReturn(List.of());

    controller.getProducts(null, " ", null, " book ", 1, 100);

    verify(productService).getProducts(null, "book");
  }

  @Test
  void getProductsShouldReturnAllProductsWhenNoSearchExists() {
    List<ProductResponse> products = List.of(mock(ProductResponse.class));
    when(productService.getAllProducts()).thenReturn(products);

    var response = controller.getProducts(
        null, null, null, null, 1, 100);

    assertEquals(1, response.getBody().get("totalItems"));
    verify(productService).getAllProducts();
  }

  @Test
  void getProductsShouldHandleNullProductList() {
    when(productService.getAllProducts()).thenReturn(null);

    var response = controller.getProducts(
        null, null, null, null, 1, 100);

    assertEquals(0, response.getBody().get("totalItems"));
    assertEquals(1, response.getBody().get("totalPages"));
    assertEquals(100, response.getBody().get("size"));
    assertTrue(((List<?>) response.getBody().get("items")).isEmpty());
  }

  @Test
  void getProductsShouldPaginateResults() {
    List<ProductResponse> products = List.of(
        mock(ProductResponse.class),
        mock(ProductResponse.class),
        mock(ProductResponse.class),
        mock(ProductResponse.class),
        mock(ProductResponse.class)
    );
    when(productService.getAllProducts()).thenReturn(products);

    var response = controller.getProducts(
        null, null, null, null, 2, 2);

    assertEquals(5, response.getBody().get("totalItems"));
    assertEquals(3, response.getBody().get("totalPages"));
    assertEquals(2, response.getBody().get("page"));
    assertEquals(2, response.getBody().get("size"));
    assertEquals(2, ((List<?>) response.getBody().get("items")).size());
    assertSame(response.getBody().get("items"),
        response.getBody().get("products"));
  }

  @Test
  void getProductsShouldUseFallbackSizeWhenSizeIsNotPositive() {
    when(productService.getAllProducts())
        .thenReturn(List.of(mock(ProductResponse.class)));

    var response = controller.getProducts(
        null, null, null, null, 1, 0);

    assertEquals(8, response.getBody().get("size"));
  }

  @Test
  void getProductsShouldReturnEmptyPageWhenPageExceedsResults() {
    when(productService.getAllProducts())
        .thenReturn(List.of(mock(ProductResponse.class)));

    var response = controller.getProducts(
        null, null, null, null, 99, 1);

    assertTrue(((List<?>) response.getBody().get("items")).isEmpty());
  }

  @Test
  void getProductByIdShouldReturnNotFoundWhenMissing() {
    when(productService.getProductById(99L)).thenReturn(null);

    var response = controller.getProductById(99L);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getProductByIdShouldReturnProductWhenFound() {
    ProductResponse product = mock(ProductResponse.class);
    when(productService.getProductById(1L)).thenReturn(product);

    var response = controller.getProductById(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(product, response.getBody());
  }

  @Test
  void getHomeDataShouldHandleNullLists() {
    when(productService.getAllProducts()).thenReturn(null);
    when(productService.getAllCategories()).thenReturn(null);

    var response = controller.getHomeData();

    assertTrue(((List<?>) response.getBody().get("bestSellers")).isEmpty());
    assertTrue(((List<?>) response.getBody().get("categories")).isEmpty());
    assertEquals("", response.getBody().get("newestImage"));
  }

  @Test
  void getHomeDataShouldFormatCategoriesAndFindNewestImage() {
    CategoryResponse category = mock(CategoryResponse.class);
    when(category.getCategoryId()).thenReturn(1L);
    when(category.getCategoryName()).thenReturn("Shoes");
    when(category.getSlug()).thenReturn("shoes");

    ProductResponse product = mock(ProductResponse.class);
    when(product.getCategoryId()).thenReturn(1L);
    when(product.getImageUrls()).thenReturn(List.of("/image/shoe.png"));

    when(productService.getAllProducts()).thenReturn(List.of(product));
    when(productService.getAllCategories()).thenReturn(List.of(category));

    var response = controller.getHomeData();

    assertEquals("/image/shoe.png", response.getBody().get("newestImage"));
    var categories = (List<?>) response.getBody().get("categories");
    assertEquals(1, categories.size());
    assertEquals("/image/shoe.png",
        ((java.util.Map<?, ?>) categories.get(0)).get("image"));
  }

  @Test
  void getHomeDataShouldUseEmptyImageWhenCategoryHasNoMatchingProduct() {
    CategoryResponse category = mock(CategoryResponse.class);
    when(category.getCategoryId()).thenReturn(2L);
    when(category.getCategoryName()).thenReturn("Books");
    when(category.getSlug()).thenReturn("books");

    ProductResponse product = mock(ProductResponse.class);
    when(product.getCategoryId()).thenReturn(1L);
    when(product.getImageUrls()).thenReturn(List.of());

    when(productService.getAllProducts()).thenReturn(List.of(product));
    when(productService.getAllCategories()).thenReturn(List.of(category));

    var response = controller.getHomeData();

    assertEquals("", response.getBody().get("newestImage"));
    var categories = (List<?>) response.getBody().get("categories");
    assertEquals("", ((java.util.Map<?, ?>) categories.get(0)).get("image"));
  }
}
