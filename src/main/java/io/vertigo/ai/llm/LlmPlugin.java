package io.vertigo.ai.llm;

import java.util.stream.Stream;

import io.vertigo.ai.llm.model.LlmChat;
import io.vertigo.ai.llm.model.VLlmMessage;
import io.vertigo.ai.llm.model.VPrompt;
import io.vertigo.ai.llm.model.VPromptContext;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.datastore.filestore.model.VFile;

public interface LlmPlugin extends Plugin {

	/**
	 * Ask the LLM something about a file.
	 *
	 * @param prompt the prompt to use
	 * @param files the files to use
	 * @return the LLM response
	 */
	VLlmMessage askOnFiles(VPrompt prompt, Stream<VFile> files);

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
	 * @param files the files to use
	 * @param context the context to use
	 * @return the new chat
	 */
	LlmChat newChat(Stream<VFile> files, VPromptContext context);

}
