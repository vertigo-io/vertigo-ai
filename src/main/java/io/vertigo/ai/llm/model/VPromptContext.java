package io.vertigo.ai.llm.model;

// voir pour le format de sortie
public class VPromptContext {
	private String constraints;
	private VPersona persona;

	/**
	 * @return the constraints
	 */
	public String getConstraints() {
		return constraints;
	}

	/**
	 * @param constraints the constraints to set
	 */
	public void setConstraints(final String constraints) {
		this.constraints = constraints;
	}

	/**
	 * @return the persona
	 */
	public VPersona getPersona() {
		return persona;
	}

	/**
	 * @param persona the persona to set
	 */
	public void setPersona(final VPersona persona) {
		this.persona = persona;
	}

}
