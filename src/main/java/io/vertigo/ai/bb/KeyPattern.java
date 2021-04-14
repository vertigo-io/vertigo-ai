package io.vertigo.ai.bb;

import io.vertigo.core.lang.Assertion;

public final class KeyPattern {

	public static String KEY_PATTERN_REGEX = "(" + BBKey.KEY_REGEX + "[\\*]?)|[\\*]";

	private final String keyPattern;

	private KeyPattern(final String keyPattern) {
		Assertion.check()
				.isNotBlank(keyPattern)
				.isTrue(keyPattern.matches(KEY_PATTERN_REGEX), "the key pattern '{0}' must contain only a-z 1-9 words separated with / and is finished by a * or nothing", keyPattern);
		//---
		this.keyPattern = keyPattern;
	}

	public String getKeyPattern() {
		return keyPattern;
	}

	public static KeyPattern of(final String keyPattern) {
		return new KeyPattern(keyPattern);
	}

}
