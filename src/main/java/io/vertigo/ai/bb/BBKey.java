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

	public BBKey subKey(final String suffix) {
		Assertion.check().isNotBlank(suffix);
		//---
		return BBKey.of(key + suffix);
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

	public static BBKey of(final String key) {
		return new BBKey(key);
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
