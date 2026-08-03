package io.github.gerardoicu.bakingandcrafts.auth;

public enum ApplicationRole {
	USER,
	ADMIN;

	public String authority() {
		return "ROLE_" + name();
	}
}
