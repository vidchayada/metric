package metric.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import metric.backend.entity.Category;
import metric.backend.entity.Product;
import metric.backend.repository.CategoryRepository;
import metric.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AdminCategoryControllerTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private AdminCategoryController controller;

  private Category visibleCategory;
  private Category hiddenCategory;

  @BeforeEach
  void setUp() {
    visibleCategory = Category.builder()
        .categoryId(1L)
        .categoryName("Shoes")
        .slug("shoes")
        .visibility("show")
        .build();

    hiddenCategory = Category.builder()
        .categoryId(2L)
        .categoryName("Summer Wear")
        .slug("summer-wear")
        .visibility("hide")
        .build();
  }

  @Test
  void getAllCategoriesShouldReturnCategoriesAndProductCounts() {
    when(categoryRepository.findAll())
        .thenReturn(List.of(visibleCategory, hiddenCategory));

    Product product = mock(Product.class);
    when(product.getCategory()).thenReturn(visibleCategory);

    when(productRepository.findAll()).thenReturn(List.of(product));

    ResponseEntity<List<Map<String, Object>>> response =
        controller.getAllCategories(null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());

    Map<String, Object> first = response.getBody().get(0);

    assertEquals(1L, first.get("id"));
    assertEquals(1L, first.get("categoryId"));
    assertEquals("Shoes", first.get("name"));
    assertEquals("Shoes", first.get("categoryName"));
    assertEquals("shoes", first.get("slug"));
    assertEquals("show", first.get("visibility"));
    assertEquals(1L, first.get("productCount"));

    Map<String, Object> second = response.getBody().get(1);
    assertEquals(0L, second.get("productCount"));
  }

  @Test
  void getAllCategoriesShouldHandleProductsWithoutCategory() {
    when(categoryRepository.findAll())
        .thenReturn(List.of(visibleCategory));

    Product productWithoutCategory = mock(Product.class);
    Product productWithAnotherCategory = mock(Product.class);
    Category anotherCategory = Category.builder()
        .categoryId(99L)
        .build();

    when(productWithoutCategory.getCategory()).thenReturn(null);
    when(productWithAnotherCategory.getCategory())
        .thenReturn(anotherCategory);

    when(productRepository.findAll())
        .thenReturn(List.of(
            productWithoutCategory,
            productWithAnotherCategory
        ));

    var response = controller.getAllCategories(null, null);

    assertEquals(0L, response.getBody().get(0).get("productCount"));
  }

  @Test
  void getAllCategoriesShouldFilterByVisibilityIgnoringCase() {
    when(categoryRepository.findAll())
        .thenReturn(List.of(visibleCategory, hiddenCategory));
    when(productRepository.findAll()).thenReturn(List.of());

    var response = controller.getAllCategories("SHOW", null);

    assertEquals(1, response.getBody().size());
    assertEquals("Shoes", response.getBody().get(0).get("name"));
  }

  @Test
  void getAllCategoriesShouldIgnoreBlankVisibilityFilter() {
    when(categoryRepository.findAll())
        .thenReturn(List.of(visibleCategory, hiddenCategory));
    when(productRepository.findAll()).thenReturn(List.of());

    var response = controller.getAllCategories("  ", null);

    assertEquals(2, response.getBody().size());
  }

  @Test
  void getAllCategoriesShouldReturnEmptyWhenVisibilityDoesNotMatch() {
    when(categoryRepository.findAll())
        .thenReturn(List.of(visibleCategory));

    var response = controller.getAllCategories("hide", null);

    assertTrue(response.getBody().isEmpty());
    verifyNoInteractions(productRepository);
  }

  @Test
  void getAllCategoriesShouldSearchByNameIgnoringCaseAndWhitespace() {
    when(categoryRepository.findAll())
        .thenReturn(List.of(visibleCategory, hiddenCategory));
    when(productRepository.findAll()).thenReturn(List.of());

    var response = controller.getAllCategories(null, "  SHO  ");

    assertEquals(1, response.getBody().size());
    assertEquals("Shoes", response.getBody().get(0).get("name"));
  }

  @Test
  void getAllCategoriesShouldSearchBySlug() {
    when(categoryRepository.findAll())
        .thenReturn(List.of(visibleCategory, hiddenCategory));
    when(productRepository.findAll()).thenReturn(List.of());

    var response = controller.getAllCategories(null, "SUMMER");

    assertEquals(1, response.getBody().size());
    assertEquals("summer-wear", response.getBody().get(0).get("slug"));
  }

  @Test
  void getAllCategoriesShouldIgnoreBlankSearch() {
    when(categoryRepository.findAll())
        .thenReturn(List.of(visibleCategory, hiddenCategory));
    when(productRepository.findAll()).thenReturn(List.of());

    var response = controller.getAllCategories(null, "  ");

    assertEquals(2, response.getBody().size());
  }

  @Test
  void getAllCategoriesShouldHandleNullNameAndSlugDuringSearch() {
    Category category = Category.builder()
        .categoryId(3L)
        .categoryName(null)
        .slug(null)
        .visibility("show")
        .build();

    when(categoryRepository.findAll()).thenReturn(List.of(category));

    var response = controller.getAllCategories(null, "missing");

    assertTrue(response.getBody().isEmpty());
    verifyNoInteractions(productRepository);
  }

  @Test
  void createCategoryShouldUseNameAndGenerateSlug() {
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = controller.createCategory(
        Map.of("name", "  Summer & WINTER Shoes!  ")
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("  Summer & WINTER Shoes!  ",
        response.getBody().getCategoryName());
    assertEquals("summer-winter-shoes", response.getBody().getSlug());
    assertEquals("show", response.getBody().getVisibility());

    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  void createCategoryShouldAcceptCategoryNameAlias() {
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = controller.createCategory(
        Map.of(
            "categoryName", "Books",
            "visibility", "hide"
        )
    );

    assertEquals("Books", response.getBody().getCategoryName());
    assertEquals("books", response.getBody().getSlug());
    assertEquals("hide", response.getBody().getVisibility());
  }

  @Test
  void createCategoryShouldPreferNameWhenBothNamesExist() {
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = controller.createCategory(
        Map.of(
            "name", "New Name",
            "categoryName", "Old Name"
        )
    );

    assertEquals("New Name", response.getBody().getCategoryName());
    assertEquals("new-name", response.getBody().getSlug());
  }

  @Test
  void createCategoryShouldTrimSlugBoundaries() {
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = controller.createCategory(
        Map.of("name", "!!!Shoes!!!")
    );

    assertEquals("shoes", response.getBody().getSlug());
  }

  @Test
  void updateCategoryShouldUpdateNameSlugAndVisibility() {
    when(categoryRepository.findById(1L))
        .thenReturn(Optional.of(visibleCategory));
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = controller.updateCategory(
        1L,
        Map.of("name", "Men's Shoes", "visibility", "hide")
    );

    assertEquals("Men's Shoes", response.getBody().getCategoryName());
    assertEquals("men-s-shoes", response.getBody().getSlug());
    assertEquals("hide", response.getBody().getVisibility());
  }

  @Test
  void updateCategoryShouldLeaveFieldsUnchangedWhenKeysAreAbsent() {
    when(categoryRepository.findById(1L))
        .thenReturn(Optional.of(visibleCategory));
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = controller.updateCategory(1L, Map.of());

    assertEquals("Shoes", response.getBody().getCategoryName());
    assertEquals("shoes", response.getBody().getSlug());
    assertEquals("show", response.getBody().getVisibility());
  }

  @Test
  void updateCategoryShouldThrowWhenCategoryDoesNotExist() {
    when(categoryRepository.findById(99L))
        .thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> controller.updateCategory(99L, Map.of())
    );

    assertEquals("Category not found with id: 99",
        exception.getMessage());
    verify(categoryRepository, never()).save(any());
  }

  @Test
  void setVisibilityPatchShouldUpdateVisibility() {
    when(categoryRepository.findById(1L))
        .thenReturn(Optional.of(visibleCategory));
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = controller.setVisibilityPatch(
        1L, Map.of("visibility", "hide")
    );

    assertEquals("hide", response.getBody().getVisibility());
  }

  @Test
  void setVisibilityPatchShouldLeaveVisibilityUnchangedWhenAbsent() {
    when(categoryRepository.findById(1L))
        .thenReturn(Optional.of(visibleCategory));
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = controller.setVisibilityPatch(1L, Map.of());

    assertEquals("show", response.getBody().getVisibility());
  }

  @Test
  void setVisibilityPatchShouldThrowWhenCategoryDoesNotExist() {
    when(categoryRepository.findById(99L))
        .thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.setVisibilityPatch(99L, Map.of())
    );
  }

  @Test
  void setVisibilityPutShouldDelegateToPatch() {
    when(categoryRepository.findById(1L))
        .thenReturn(Optional.of(visibleCategory));
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = controller.setVisibilityPut(
        1L, Map.of("visibility", "hide")
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("hide", response.getBody().getVisibility());

    verify(categoryRepository).findById(1L);
    verify(categoryRepository).save(visibleCategory);
  }

  @Test
  void deleteCategoryShouldReturnNoContent() {
    doNothing().when(categoryRepository).deleteById(1L);

    ResponseEntity<Void> response = controller.deleteCategory(1L);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNull(response.getBody());
    verify(categoryRepository).deleteById(1L);
  }
}
