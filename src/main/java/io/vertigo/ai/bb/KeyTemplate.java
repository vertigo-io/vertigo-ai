package io.vertigo.ai.bb;

import io.vertigo.core.lang.Assertion;

public final class KeyTemplate {

	private final String keyTemplate;

	private KeyTemplate(final String keyTemplate) {
		Assertion.check()
				.isNotNull(keyTemplate);
		//---
		this.keyTemplate = keyTemplate;
	}

	public String getKeyTemplate() {
		return keyTemplate;
	}

	public KeyTemplate indent(final String prefix) {
		Assertion.check().isNotBlank(prefix);
		//---
		return KeyTemplate.of(prefix + keyTemplate);
	}

	public KeyTemplate outdent(final String prefix) {
		Assertion.check()
				.isNotBlank(prefix)
				.isTrue(keyTemplate.startsWith(prefix), "To outdent the keyTemplate '{0}' it must starts with the provided prefix '{1}' ", keyTemplate, prefix);
		//---
		return KeyTemplate.of(keyTemplate.substring(0, prefix.length() - 1));
	}

	public static KeyTemplate of(final String keyTemplate) {
		return new KeyTemplate(keyTemplate);
	}

}
