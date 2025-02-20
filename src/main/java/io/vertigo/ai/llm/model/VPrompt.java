package io.vertigo.ai.llm.model;

import io.vertigo.core.lang.Assertion;

// voir pour le format de sortie
public class VPrompt {
	private final String instructions;
	private final VPromptContext context;

	VPrompt(final String instructions, final VPromptContext context) {
		Assertion.check()
				.isNotBlank(instructions)
				.isNotNull(context);
		//---
		this.instructions = instructions;
		this.context = context;
	}

	/**
	 * @return the instructions
	 */
	public String getInstructions() {
		return instructions;
	}

	/**
	 * @return the context
	 */
	public VPromptContext getContext() {
		return context;
	}

	/**
	 * Creates the builder.
	 * @return the builder
	 */
	public static VPromptBuilder builder(String instructions) {
		return new VPromptBuilder(instructions);
	}
}
