package metric.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import metric.backend.dto.CreateProductRequest;
import metric.backend.dto.ProductResponse;
import metric.backend.service.AdminProductService;
import metric.backend.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AdminProductControllerTest {

  @Mock AdminProductService adminProductService;
  @Mock ProductService productService;

  @InjectMocks AdminProductController controller;

  @Test
  void getAllProductsShouldPassFilters() {
    List<ProductResponse> products = List.of(mock(ProductResponse.class));
    when(productService.getProducts(2L, "shoe")).thenReturn(products);

    var response = controller.getAllProducts(2L, "shoe");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(products, response.getBody());
  }

  @Test
  void getAllProductsShouldAcceptNullFilters() {
    when(productService.getProducts(null, null)).thenReturn(List.of());

    var response = controller.getAllProducts(null, null);

    assertTrue(response.getBody().isEmpty());
  }

  @Test
  void createProductShouldReturnCreated() {
    CreateProductRequest request = mock(CreateProductRequest.class);
    List<MultipartFile> images = List.of(
        new MockMultipartFile("images", "shoe.png",
            "image/png", new byte[]{1, 2})
    );
    ProductResponse expected = mock(ProductResponse.class);

    when(adminProductService.createProduct(request, images))
        .thenReturn(expected);

    var response = controller.createProduct(request, images);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertSame(expected, response.getBody());
  }

  @Test
  void createProductShouldAllowNullImages() {
    CreateProductRequest request = mock(CreateProductRequest.class);
    ProductResponse expected = mock(ProductResponse.class);

    when(adminProductService.createProduct(request, null))
        .thenReturn(expected);

    var response = controller.createProduct(request, null);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertSame(expected, response.getBody());
  }

  @Test
  void updateProductShouldReturnUpdatedProduct() {
    CreateProductRequest request = mock(CreateProductRequest.class);
    ProductResponse expected = mock(ProductResponse.class);

    when(adminProductService.updateProduct(3L, request, null))
        .thenReturn(expected);

    var response = controller.updateProduct(3L, request, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(expected, response.getBody());
  }

  @Test
  void deleteProductShouldReturnNoContent() {
    doNothing().when(adminProductService).deleteProduct(3L);

    var response = controller.deleteProduct(3L);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNull(response.getBody());
    verify(adminProductService).deleteProduct(3L);
  }
}