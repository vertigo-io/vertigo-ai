package io.vertigo.ai.nlu;

import java.util.List;

import io.vertigo.core.lang.Assertion;

/**
 * Result of the recognition of a sentence by the NLU engine.
 *
 * @author skerdudou
 */
public final class NluResult {
	private final String rawSentence;
	private final List<ScoredIntent> scoredIntents;

	public NluResult(final String rawSentence, final List<ScoredIntent> scoredIntents) {
		Assertion.check()
				.isNotBlank(rawSentence)
				.isNotNull(scoredIntents);
		//---
		this.rawSentence = rawSentence;
		this.scoredIntents = scoredIntents;
	}

	/**
	 * @return the rawSentence
	 */
	public String getRawSentence() {
		return rawSentence;
	}

	/**
	 * @return the intentClassificationList
	 */
	public List<ScoredIntent> getScoredIntents() {
		return scoredIntents;
	}
}
