package com.example.foundation.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

import com.example.foundation.health.HealthController;

@WebMvcTest({ HealthController.class, AuthController.class, CsrfController.class })
@Import({ SecurityConfiguration.class, AuthExceptionHandler.class, RefreshCookieService.class })
@TestPropertySource(properties = "app.auth.jwt-secret=test-secret-that-is-not-for-real-use")
class AuthSecurityMvcTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private UserAccountRepository userAccounts;

	@Autowired
	private AuthProperties authProperties;

	@Test
	void healthAllowsAnonymousAccess() throws Exception {
		mockMvc.perform(get("/api/v1/health"))
			.andExpect(status().isOk())
			.andExpect(content().json("""
				{"healthy":true}
				"""));
	}

	@Test
	void protectedEndpointReturnsUnauthorizedWithoutToken() throws Exception {
		mockMvc.perform(get("/api/v1/auth/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void roleRestrictedEndpointReturnsForbiddenForInsufficientRole() throws Exception {
		mockMvc.perform(get("/api/v1/auth/admin-check")
				.with(jwt().authorities(new SimpleGrantedAuthority(ApplicationRole.USER.authority()))))
			.andExpect(status().isForbidden());
	}

	@Test
	void roleRestrictedEndpointRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/auth/admin-check"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void roleRestrictedEndpointAllowsAdminRole() throws Exception {
		mockMvc.perform(get("/api/v1/auth/admin-check")
				.with(jwt().authorities(new SimpleGrantedAuthority(ApplicationRole.ADMIN.authority()))))
			.andExpect(status().isOk());
	}

	@Test
	void csrfEndpointMaterializesReadableCookie() throws Exception {
		mockMvc.perform(get("/api/v1/auth/csrf"))
			.andExpect(status().isNoContent())
			.andExpect(cookie().exists("XSRF-TOKEN"));
	}

	@Test
	void authenticationPostRejectsMissingCsrf() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com","password":"secret"}
					"""))
			.andExpect(status().isForbidden());
	}

	@Test
	void loginWithValidCsrfReturnsAccessTokenAndRefreshCookie() throws Exception {
		AuthResponse response = new AuthResponse(
			"access",
			Instant.parse("2026-08-01T00:15:00Z"),
			new AuthenticatedUserResponse(
				UUID.fromString("8e31c835-756d-4609-a6d2-bf20d1505be3"),
				"user@example.com",
				Set.of(ApplicationRole.USER)
			)
		);
		when(authService.login(new LoginRequest("user@example.com", "secret")))
			.thenReturn(new AuthService.LoginResult(
				response,
				"refresh",
				Instant.parse("2026-08-08T00:00:00Z"),
				Instant.parse("2026-08-01T00:00:00Z")
			));

		mockMvc.perform(post("/api/v1/auth/login")
				.with(csrf().asHeader())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"user@example.com","password":"secret"}
					"""))
			.andExpect(status().isOk())
			.andExpect(cookie().exists(authProperties.refreshCookie().name()))
			.andExpect(content().json("""
				{
					"accessToken":"access",
					"accessTokenExpiresAt":"2026-08-01T00:15:00Z",
					"user":{"email":"user@example.com","roles":["USER"]}
				}
				"""));
	}

	@Test
	void logoutRequiresCsrfBeforeReturningNoContent() throws Exception {
		mockMvc.perform(post("/api/v1/auth/logout")
				.cookie(new Cookie(authProperties.refreshCookie().name(), "refresh")))
			.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/v1/auth/logout")
				.with(csrf().asHeader())
				.cookie(new Cookie(authProperties.refreshCookie().name(), "refresh")))
			.andExpect(status().isNoContent());
	}
}
