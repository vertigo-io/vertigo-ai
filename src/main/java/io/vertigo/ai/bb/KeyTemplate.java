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

	public static KeyTemplate of(final String keyTemplate) {
		return new KeyTemplate(keyTemplate);
	}

}
