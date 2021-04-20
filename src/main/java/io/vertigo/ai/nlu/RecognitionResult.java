package io.vertigo.ai.nlu;

import java.util.List;

import io.vertigo.core.lang.Assertion;

/**
 * Result of the analysis of a sentence by the NLU engine.
 *
 * @author skerdudou
 */
public final class RecognitionResult {
	private final String rawSentence;
	private final List<ScoredIntent> scoredIntents;

	public RecognitionResult(final String rawSentence, final List<ScoredIntent> scoredIntents) {
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
