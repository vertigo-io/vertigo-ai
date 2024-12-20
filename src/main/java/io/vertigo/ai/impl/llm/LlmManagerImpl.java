package io.vertigo.ai.impl.llm;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import io.vertigo.ai.llm.LlmManager;
import io.vertigo.ai.llm.LlmPlugin;
import io.vertigo.ai.llm.model.VPersona;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Manager for Large Language Models usage.
 *
 * @author skerdudou
 */
public class LlmManagerImpl implements LlmManager {
	private final LlmPlugin llmPlugin;

	/**
	 * Constructor.
	 *
	 * @param llmPlugin the plugin to use
	 */
	@Inject
	public LlmManagerImpl(final LlmPlugin llmPlugin) {
		Assertion.check().isNotNull(llmPlugin);
		//---
		this.llmPlugin = llmPlugin;
	}

	@Override
	public String promptOnFiles(final VPrompt prompt, final VFile... files) {
		return llmPlugin.promptOnFiles(prompt, Arrays.stream(files));
	}

	@Override
	public String promptOnFiles(final VPrompt prompt, final Collection<VFile> files) {
		return llmPlugin.promptOnFiles(prompt, files.stream());
	}

	@Override
	public String summarize(final VFile file) {
		final var prompt = new VPrompt(StandardPrompts.SUMMARY_PROMPT, null);
		final VFile[] files = { file };
		return promptOnFiles(prompt, files);
	}

	@Override
	public String summarize(final VFile file, final VPersona persona) {
		final var prompt = new VPrompt(StandardPrompts.SUMMARY_PROMPT, persona);
		final VFile[] files = { file };
		return promptOnFiles(prompt, files);
	}

	@Override
	public String describe(final VFile file) {
		final var prompt = new VPrompt(StandardPrompts.DESCRIBE_PROMPT, null);
		final VFile[] files = { file };
		return promptOnFiles(prompt, files);
	}

	@Override
	public String describe(final VFile file, final VPersona persona) {
		final var prompt = new VPrompt(StandardPrompts.DESCRIBE_PROMPT, persona);
		final VFile[] files = { file };
		return promptOnFiles(prompt, files);
	}

}
