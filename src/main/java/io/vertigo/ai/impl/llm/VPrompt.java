package io.vertigo.ai.impl.llm;

import io.vertigo.ai.llm.model.VPersona;

// voir pour le format de sortie
public record VPrompt(String instructions, String constraints, VPersona persona) {

}
