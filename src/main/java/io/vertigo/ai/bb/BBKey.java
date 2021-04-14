package io.vertigo.ai.bb;

import io.vertigo.core.lang.Assertion;

public final class BBKey {

	public static String KEY_REGEX = "[a-z]+(/[a-z0-9]*)*";

	private final String key;

	private BBKey(final String key) {
		Assertion.check()
				.isNotBlank(key)
				.isTrue(key.matches(KEY_REGEX), "the key '{0}' must contain only a-z 1-9 words separated with /", key);
		//---
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public boolean startsWith(final String prefix) {
		return key.startsWith(prefix);
	}

	public static BBKey of(final String key) {
		return new BBKey(key);
	}

}
