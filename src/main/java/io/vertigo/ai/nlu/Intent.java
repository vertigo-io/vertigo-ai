package io.vertigo.ai.nlu;

import io.vertigo.core.lang.Assertion;

/**
 * An intent is the classification of what a user wants to say.
 *
 * @author skerdudou
 */
public final class Intent {
	private final String code;

	private Intent(final String code) {
		Assertion.check().isNotBlank(code);
		//--
		this.code = code;
	}

	public static Intent of(final String code) {
		return new Intent(code);
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

}
