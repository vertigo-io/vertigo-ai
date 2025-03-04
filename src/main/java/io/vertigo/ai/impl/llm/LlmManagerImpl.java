package io.vertigo.ai.impl.llm;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import io.vertigo.ai.llm.LlmChat;
import io.vertigo.ai.llm.LlmManager;
import io.vertigo.ai.llm.LlmPlugin;
import io.vertigo.ai.llm.model.VLlmMessage;
import io.vertigo.ai.llm.model.VPrompt;
import io.vertigo.ai.llm.model.VPromptContext;
import io.vertigo.ai.llm.model.rag.VLlmDocumentSource;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.daemon.definitions.DaemonDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;

/**
 * Manager for Large Language Models usage.
 *
 * @author skerdudou
 */
public class LlmManagerImpl implements LlmManager {
	public static final String LLM_CATEGORY = "llm";

	private static final Map<UUID, LlmChat> CHATS = new HashMap<>();

	private final LlmPlugin llmPlugin;

	@Inject
	private AnalyticsManager analyticsManager;

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
	public VLlmDocumentSource getPersistedDocumentSource() {
		return llmPlugin.getPersistedDocumentSource();
	}

	@Override
	public VLlmDocumentSource getTemporaryDocumentSource() {
		return llmPlugin.getTemporaryDocumentSource();
	}

	@Override
	public VLlmMessage askOnFiles(final VPrompt prompt, final VLlmDocumentSource documentSource) {
		return analyticsManager.traceWithReturn(LLM_CATEGORY, "askFiles",
				tracer -> llmPlugin.askOnFiles(prompt, documentSource));
	}

	@Override
	public VLlmMessage ask(final VPrompt prompt) {
		return analyticsManager.traceWithReturn(LLM_CATEGORY, "ask",
				tracer -> llmPlugin.ask(prompt));
	}

	@Override
	public <T> T ask(final VPrompt prompt, final Class<T> clazz) {
		return analyticsManager.traceWithReturn(LLM_CATEGORY, "ask",
				tracer -> llmPlugin.ask(prompt, clazz));
	}

	@Override
	public LlmChat initChat() {
		return initChat(null, new VPromptContext());
	}

	@Override
	public LlmChat initChat(final VLlmDocumentSource documentSource, final VPromptContext context) {
		Assertion.check()
				.isNotNull(documentSource)
				.isNotNull(context);
		//---
		return analyticsManager.traceWithReturn(LLM_CATEGORY, "newChat",
				tracer -> {
					final var newChat = llmPlugin.newChat(documentSource, context);
					tracer.setMetadata("chatId", newChat.getId().toString());
					CHATS.put(newChat.getId(), newChat);
					return newChat;
				});
	}

	@Override
	public LlmChat getChat(final UUID id) {
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
