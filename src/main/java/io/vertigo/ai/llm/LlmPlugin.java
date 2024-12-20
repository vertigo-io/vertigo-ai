package io.vertigo.ai.llm;

import java.util.stream.Stream;

import io.vertigo.ai.impl.llm.VPrompt;
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
	String promptOnFiles(VPrompt prompt, Stream<VFile> files);

}
