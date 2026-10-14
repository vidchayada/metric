package metric.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import java.util.Optional;
import metric.backend.entity.Role;
import metric.backend.entity.User;
import metric.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

  private JwtTokenProvider jwtTokenProvider;
  private UserRepository userRepository;
  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = mock(JwtTokenProvider.class);
    userRepository = mock(UserRepository.class);
    filter = new JwtAuthenticationFilter(jwtTokenProvider, userRepository);

    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldAuthenticateUserWithValidBearerToken() throws Exception {
    MockHttpServletRequest request = request("/api/cart");
    request.addHeader("Authorization", "Bearer valid-token");

    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    User user = User.builder()
        .userId(1L)
        .username("alice")
        .role(Role.ADMIN)
        .build();

    when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
    when(jwtTokenProvider.getUsername("valid-token")).thenReturn("alice");
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

    filter.doFilter(request, response, chain);

    var authentication =
        SecurityContextHolder.getContext().getAuthentication();

    assertNotNull(authentication);
    assertInstanceOf(
        UsernamePasswordAuthenticationToken.class,
        authentication);
    assertSame(user, authentication.getPrincipal());
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

    verify(chain).doFilter(request, response);
    verify(userRepository).findByUsername("alice");
  }

  @Test
  void shouldCreateUserAuthorityForUserRole() throws Exception {
    MockHttpServletRequest request = request("/api/cart");
    request.addHeader("Authorization", "Bearer user-token");

    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    User user = User.builder()
        .username("bob")
        .role(Role.USER)
        .build();

    when(jwtTokenProvider.validateToken("user-token")).thenReturn(true);
    when(jwtTokenProvider.getUsername("user-token")).thenReturn("bob");
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

    filter.doFilter(request, response, chain);

    var authentication =
        SecurityContextHolder.getContext().getAuthentication();

    assertNotNull(authentication);
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
  }

  @Test
  void shouldContinueChainWhenAuthorizationHeaderIsMissing() throws Exception {
    MockHttpServletRequest request = request("/api/cart");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verifyNoInteractions(jwtTokenProvider, userRepository);
    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldIgnoreAuthorizationHeaderWithoutBearerPrefix() throws Exception {
    MockHttpServletRequest request = request("/api/cart");
    request.addHeader("Authorization", "Basic abc123");

    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verifyNoInteractions(jwtTokenProvider, userRepository);
    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldIgnoreBlankBearerToken() throws Exception {
    MockHttpServletRequest request = request("/api/cart");
    request.addHeader("Authorization", "Bearer    ");

    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verifyNoInteractions(jwtTokenProvider, userRepository);
    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldContinueChainWhenTokenIsInvalid() throws Exception {
    MockHttpServletRequest request = request("/api/cart");
    request.addHeader("Authorization", "Bearer invalid-token");

    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    when(jwtTokenProvider.validateToken("invalid-token"))
        .thenReturn(false);

    filter.doFilter(request, response, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(jwtTokenProvider, never()).getUsername(anyString());
    verifyNoInteractions(userRepository);
    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldContinueChainWhenUserDoesNotExist() throws Exception {
    MockHttpServletRequest request = request("/api/cart");
    request.addHeader("Authorization", "Bearer valid-token");

    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
    when(jwtTokenProvider.getUsername("valid-token")).thenReturn("missing");
    when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

    filter.doFilter(request, response, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldSkipLoginEndpoint() throws Exception {
    assertSkipped("/api/auth/login");
  }

  @Test
  void shouldSkipRegisterEndpoint() throws Exception {
    assertSkipped("/api/auth/register");
  }

  @Test
  void shouldSkipSubscriberEndpoints() throws Exception {
    assertSkipped("/api/subscribers");
    assertSkipped("/api/subscribers/subscribe");
  }

  @Test
  void shouldSkipImageEndpoints() throws Exception {
    assertSkipped("/image/products/1.png");
    assertSkipped("/images/1.png");
    assertSkipped("/uploads/1.png");
  }

  @Test
  void shouldSkipErrorEndpoints() throws Exception {
    assertSkipped("/error");
    assertSkipped("/error/404");
  }

  @Test
  void shouldNotSkipOtherEndpoints() throws Exception {
    MockHttpServletRequest request = request("/api/cart");
    request.addHeader("Authorization", "Bearer token");

    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    when(jwtTokenProvider.validateToken("token")).thenReturn(false);

    filter.doFilter(request, response, chain);

    verify(jwtTokenProvider).validateToken("token");
    verify(chain).doFilter(request, response);
  }

  private MockHttpServletRequest request(String uri) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(uri);
    return request;
  }

  private void assertSkipped(String uri) throws Exception {
    MockHttpServletRequest request = request(uri);
    request.addHeader("Authorization", "Bearer token");

    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verifyNoInteractions(jwtTokenProvider, userRepository);
    verify(chain).doFilter(request, response);
  }
}
