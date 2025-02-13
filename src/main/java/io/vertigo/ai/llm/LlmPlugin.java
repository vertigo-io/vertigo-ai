package io.vertigo.ai.llm;

import java.util.stream.Stream;

import io.vertigo.ai.llm.model.LlmChat;
import io.vertigo.ai.llm.model.VLlmResult;
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
	VLlmResult promptOnFiles(VPrompt prompt, Stream<VFile> files);

	/**
	 * Create a new chat.
	 *
	 * @param files the files to use
	 * @param context the context to use
	 * @return the new chat
	 */
	LlmChat newChat(Stream<VFile> files, VPromptContext context);

}
