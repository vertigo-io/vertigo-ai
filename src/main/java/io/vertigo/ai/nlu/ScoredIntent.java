package io.vertigo.ai.nlu;

import io.vertigo.core.lang.Assertion;

/**
 * The result of an intent classification.
 *
 * @author skerdudou
 */
public final class ScoredIntent {
	private final NluIntent intent;
	private final double accuracy;

	public ScoredIntent(final NluIntent intent, final Double accuracy) {
		Assertion.check()
				.isNotNull(intent)
				.isNotNull(accuracy);
		//--
		this.intent = intent;
		this.accuracy = accuracy;
	}

	/**
	 * @return the intent
	 */
	public NluIntent getIntent() {
		return intent;
	}

	/**
	 * @return the accuracy
	 */
	public double getAccuracy() {
		return accuracy;
	}
}
