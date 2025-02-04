package io.vertigo.ai.impl.llm;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import io.vertigo.ai.llm.LlmManager;
import io.vertigo.ai.llm.LlmPlugin;
import io.vertigo.ai.llm.model.LlmChat;
import io.vertigo.ai.llm.model.VLlmResult;
import io.vertigo.ai.llm.model.VPrompt;
import io.vertigo.core.daemon.definitions.DaemonDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Manager for Large Language Models usage.
 *
 * @author skerdudou
 */
public class LlmManagerImpl implements LlmManager {
	private static final Random RANDOM = new Random();
	private static final Map<Long, LlmChat> CHATS = new HashMap<>();

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
	public VLlmResult promptOnFiles(final VPrompt prompt, final VFile... files) {
		return llmPlugin.promptOnFiles(prompt, Arrays.stream(files));
	}

	@Override
	public VLlmResult promptOnFiles(final VPrompt prompt, final Collection<VFile> files) {
		return llmPlugin.promptOnFiles(prompt, files.stream());
	}

	@Override
	public LlmChat initChat() {
		return initChat(Collections.emptyList());
	}

	@Override
	public LlmChat initChat(final Collection<VFile> files) {
		// To improve security, we can add the sessionId to the key if present
		final var newId = RANDOM.nextLong();
		final var newChat = llmPlugin.newChat(newId, files.stream());

		CHATS.put(newId, newChat);

		return newChat;
	}

	@Override
	public LlmChat getChat(final Long id) {
		return CHATS.get(id);
	}

	public static final class LlmChatDaemon implements SimpleDefinitionProvider {

		@Override
		public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
			final int purgePeriod = 5 * 60; // 5 minutes

			return Collections.singletonList(new DaemonDefinition("DmnLlmPurgeChats", () -> () -> {
				final var oldestInstant = Instant.now().minusSeconds(20L * 60L); // 20 minutes of inactivity
				CHATS.entrySet().removeIf(entry -> entry.getValue().getLastUse().isBefore(oldestInstant));
			}, purgePeriod));
		}

	}
}
