package io.vertigo.ai.llm.model;

// voir pour le format de sortie
public class VPrompt {
	private final String instructions;
	private final VPromptContext context;

	public VPrompt(final String instructions) {
		this.instructions = instructions;
		context = new VPromptContext();
	}

	public VPrompt(final String instructions, final VPromptContext context) {
		this.instructions = instructions;
		this.context = context;
	}

	public VPrompt withPersona(final VPersona persona) {
		context.setPersona(persona);
		return this;
	}

	public VPrompt withConstraints(final String constraints) {
		context.setConstraints(constraints);
		return this;
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

}
