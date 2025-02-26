package io.vertigo.ai.llm.model;

import java.util.List;

/**
 * Result of a Llm query.
 * Resutl can be in text, markdown or html format.
 */
public interface VLlmMessage {

	/**
	 * Get the result in text format, without any markdown or html.
	 *
	 * @return the result in text format
	 */
	String getText();

	/**
	 * Get the result in markdown format.
	 *
	 * @return the result in markdown format
	 */
	String getMarkdown();

	/**
	 * Get the result in html format.
	 *
	 * @return the result in html format
	 */
	String getHtml();

	/**
	 * Get the sources of the result.
	 *
	 * @return the sources of the result
	 */
	List<String> getSources(); // TODO : improve this later to be more than just a list of file names

}
