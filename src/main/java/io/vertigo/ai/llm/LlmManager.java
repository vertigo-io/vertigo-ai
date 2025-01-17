package io.vertigo.ai.llm;

import java.util.Collection;

import io.vertigo.ai.impl.llm.VLlmResult;
import io.vertigo.ai.impl.llm.VPrompt;
import io.vertigo.ai.llm.model.VPersona;
import io.vertigo.core.node.component.Manager;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Manager for Large Language Models usage.
 *
 * @author skerdudou
 */
public interface LlmManager extends Manager {

	/**
	 * Ask the LLM something about a file.
	 *
	 * @param prompt the prompt to use
	 * @param files the files to use
	 * @return the LLM response
	 */
	VLlmResult promptOnFiles(VPrompt prompt, VFile... files);

	/**
	 * Ask the LLM something about a file.
	 *
	 * @param prompt the prompt to use
	 * @param files the files to use
	 * @return the LLM response
	 */
	VLlmResult promptOnFiles(VPrompt prompt, Collection<VFile> files);

	/**
	 * Summarize a file.
	 *
	 * @param file the file to summarize
	 * @return the summarized file
	 */
	VLlmResult summarize(VFile file);

	/**
	 * Summarize a file.
	 *
	 * @param file the file to summarize
	 * @param persona the persona to use
	 * @return the summarized file
	 */
	VLlmResult summarize(VFile file, VPersona persona);

}
