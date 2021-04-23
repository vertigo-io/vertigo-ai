package io.vertigo.ai.bb;

import io.vertigo.core.lang.Assertion;

public final class BBKey {

	public static String KEY_REGEX = "/[a-z]+(/[a-z0-9]*)*";

	private final String key;

	private BBKey(final String key) {
		Assertion.check()
				.isNotBlank(key)
				.isTrue(key.matches(KEY_REGEX), "the key '{0}' must contain only a-z 1-9 words separated with /", key);
		//---
		this.key = key;
	}

	public String key() {
		return key;
	}

	public static BBKey of(final String key) {
		return new BBKey(key);
	}

	public static BBKey of(final BBKey rootKey, final String key) {
		Assertion.check()
				.isNotNull(rootKey)
				.isNotBlank(key);
		//---
		return BBKey.of(rootKey.key() + key);
	}

	public BBKey add(final BBKey otherKey) {
		return BBKey.of(key + otherKey.key);
	}

	public BBKey head() {
		Assertion.check()
				.isTrue(key.charAt(0) == '/', "Key {0} doesn't start with the first char '/'", key);
		final int nextSlash = key.indexOf('/', 1);
		return nextSlash < 0 ? this : BBKey.of(key.substring(0, nextSlash));
	}

	public BBKey tail() {
		final int lastSlash = key.lastIndexOf('/');
		return lastSlash == 0 ? this : BBKey.of(key.substring(lastSlash));
	}

	public BBKey indent(final String prefix) {
		Assertion.check().isNotBlank(prefix);
		//---
		return BBKey.of(prefix + key);
	}

	public BBKey outdent(final String prefix) {
		Assertion.check()
				.isNotBlank(prefix)
				.isTrue(key.startsWith(prefix), "To outdent the key '{0}' it must starts with the provided prefix '{1}' ", key, prefix);
		//---
		return BBKey.of(key.substring(0, prefix.length() - 1));
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof BBKey && key.equals(((BBKey) obj).key);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

}
