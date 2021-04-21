package io.vertigo.ai.nlu;

import io.vertigo.core.lang.Assertion;

/**
 * An intent is what a user wants to say.
 * 
 * @author skerdudou
 */
public final class NluIntent {
	private final String code;

	private NluIntent(final String code) {
		Assertion.check().isNotBlank(code);
		//---
		this.code = code;
	}

	public static NluIntent of(final String code) {
		return new NluIntent(code);
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof NluIntent && code.equals(((NluIntent) obj).code);
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}
}
