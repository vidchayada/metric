package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResponseTest {

  @Test
  void shouldSupportConstructorsAndAccessors() {
    assertNull(new PageResponse<String>().getItems());

    var response = new PageResponse<>(List.of("A", "B"), 1, 3);

    assertEquals(List.of("A", "B"), response.getItems());
    assertEquals(1, response.getPage());
    assertEquals(3, response.getTotalPages());
  }

  @Test
  void shouldSupportSetters() {
    var response = new PageResponse<String>();

    response.setItems(List.of("Product"));
    response.setPage(2);
    response.setTotalPages(5);

    assertEquals(List.of("Product"), response.getItems());
    assertEquals(2, response.getPage());
    assertEquals(5, response.getTotalPages());
  }

  @Test
  void shouldAllowEmptyPage() {
    var response = new PageResponse<>(List.of(), 1, 1);

    assertTrue(response.getItems().isEmpty());
    assertEquals(1, response.getPage());
    assertEquals(1, response.getTotalPages());
  }
}
