package io.github.gerardoicu.bakingandcrafts.auth;

import java.time.Instant;

public record AuthResponse(
	String accessToken,
	Instant accessTokenExpiresAt,
	AuthenticatedUserResponse user
) {
}
