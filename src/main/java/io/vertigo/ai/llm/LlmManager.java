package io.vertigo.ai.llm;

import java.util.Collection;

import io.vertigo.ai.llm.model.LlmChat;
import io.vertigo.ai.llm.model.VLlmResult;
import io.vertigo.ai.llm.model.VPrompt;
import io.vertigo.ai.llm.model.VPromptContext;
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
	 * Create a new chat.
	 *
	 * @return the new chat
	 */
	LlmChat initChat();

	/**
	 * Create a new chat.
	 *
	 * @param files the files to use in the context of the chat
	 * @param context the context to use (Persona, ...)
	 * @return the new chat
	 */
	LlmChat initChat(final Collection<VFile> files, VPromptContext context);

	/**
	 * Get a chat by its id.
	 *
	 * @param id the id of the chat
	 * @return the chat
	 */
	LlmChat getChat(final Long id);

}
