package io.vertigo.ai.llm.model;

public class VPromptContext {
	private String constraints;
	private String format;
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
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(final String format) {
		this.format = format;
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
