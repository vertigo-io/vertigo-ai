package io.vertigo.ai.llm;

import io.vertigo.ai.llm.model.VLlmMessage;
import io.vertigo.ai.llm.model.VPrompt;
import io.vertigo.ai.llm.model.VPromptContext;
import io.vertigo.ai.llm.model.rag.VLlmDocumentSource;
import io.vertigo.core.node.component.Plugin;

public interface LlmPlugin extends Plugin {

	/**
	 * Ask the LLM something about a file.
	 *
	 * @param prompt the prompt to use
	 * @param documentSource the files to use
	 * @return the LLM response
	 */
	VLlmMessage askOnFiles(VPrompt prompt, VLlmDocumentSource documentSource);

	/**
	 * Ask the LLM something.
	 *
	 * @param prompt the prompt to use
	 * @return the LLM response
	 */
	default VLlmMessage ask(final VPrompt prompt) {
		return askOnFiles(prompt, null);
	}

	/**
	 * Ask the LLM something.
	 *
	 * @param prompt the prompt to use
	 * @param clazz the class of the response
	 * @return the LLM response, reported in the class structure
	 */
	<T extends Object> T ask(VPrompt prompt, Class<T> clazz);

	/**
	 * Create a new chat.
	 *
	 * @param documentSource the files to use
	 * @param context the context to use
	 * @return the new chat
	 */
	LlmChat newChat(VLlmDocumentSource documentSource, VPromptContext context);

	/**
	 * Get the persisted document source.
	 *
	 * @return the document source
	 */
	VLlmDocumentSource getPersistedDocumentSource();

	/**
	 * Get the temporary document source.
	 *
	 * @return the document source
	 */
	VLlmDocumentSource getTemporaryDocumentSource();

}
