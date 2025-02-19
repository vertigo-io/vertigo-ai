package io.vertigo.ai.llm.model;

import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;

public final class VPromptBuilder implements Builder<VPrompt>{
	private final String myInstructions;
	private final VPromptContext myContext;


	VPromptBuilder(final String instructions) {
		Assertion.check().isNotBlank(instructions);
		//---
		this.myInstructions = instructions;
		this.myContext = new VPromptContext();
	}

	public VPromptBuilder withPersona(final VPersona persona) {
		myContext.setPersona(persona);
		return this;
	}

	public VPromptBuilder withConstraints(final String constraints) {
		myContext.setConstraints(constraints);
		return this;
	}
	
	public VPrompt build() {
		return new VPrompt(myInstructions, myContext);
	}

}
