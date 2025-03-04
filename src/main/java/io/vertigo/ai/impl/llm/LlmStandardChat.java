package io.vertigo.ai.impl.llm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.vertigo.ai.llm.LlmChat;
import io.vertigo.ai.llm.model.VChatMessage;
import io.vertigo.ai.llm.model.VLlmMessage;
import io.vertigo.ai.llm.model.VLlmMessageStreamConfig;
import io.vertigo.ai.llm.model.VPromptContext;
import io.vertigo.ai.llm.model.rag.VLlmDocumentSource;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.trace.TraceSpan;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;

/**
 * A chat session.
 */
public abstract class LlmStandardChat implements LlmChat {

	protected final UUID id;
	protected Instant lastUse;
	protected final List<VChatMessage> messages;
	protected final VLlmDocumentSource documentSource;
	protected final VPromptContext context;

	private final AnalyticsManager analyticsManager;

	protected LlmStandardChat(final VLlmDocumentSource documentSource, final VPromptContext context) {
		id = UUID.randomUUID();
		lastUse = Instant.now();
		messages = new ArrayList<>();
		this.documentSource = documentSource;
		this.context = context == null ? new VPromptContext() : context;

		analyticsManager = Node.getNode().getComponentSpace().resolve(AnalyticsManager.class);
	}

	/**
	 * @return the chat id
	 */
	@Override
	public final UUID getId() {
		return id;
	}

	/**
	 * @return the last use date
	 */
	@Override
	public final Instant getLastUse() {
		return lastUse;
	}

	/**
	 * @return the full conversation
	 */
	@Override
	public final List<VChatMessage> getMessages() {
		return messages;
	}

	/**
	 * @return the context
	 */
	@Override
	public final VPromptContext getContext() {
		return context;
	}

	/**
	 * @return the documentSource
	 */
	@Override
	public VLlmDocumentSource getDocumentSource() {
		return documentSource;
	}

	/**
	 * Chat with the assistant.
	 *
	 * @param instructions the instructions
	 * @return the chat message
	 */
	@Override
	public final VChatMessage chat(final String instructions) {
		Assertion.check().isNotNull(instructions);
		//---
		return analyticsManager.traceWithReturn(LlmManagerImpl.LLM_CATEGORY, "chat", tracer -> {
			tracer.setMetadata("chatId", id.toString());

			lastUse = Instant.now();
			final var result = doChat(instructions);
			final var chatMessage = new VChatMessage(result, lastUse, false);
			messages.add(chatMessage);
			return chatMessage;
		});
	}

	/**
	 * Chat with the assistant, streaming the results.
	 *
	 * @param instructions the instructions
	 * @param streamConfig the stream configuration, with handlers for new tokens, execution end and errors
	 */
	@Override
	public final void chatStream(final String instructions, final VLlmMessageStreamConfig<VChatMessage> streamConfig) {
		Assertion.check()
				.isNotNull(streamConfig)
				.isNotNull(instructions);
		//---
		final var beginStream = Instant.now();
		lastUse = beginStream;
		doChatStream(instructions, new VLlmMessageStreamConfig<>(
				streamConfig.tokenHandler(),
				r -> streamConfig.partialMessageHandler().accept(new VChatMessage(r, beginStream, false)),
				r -> {
					analyticsManager.addSpan(
							TraceSpan
									.builder(LlmManagerImpl.LLM_CATEGORY, "chatStream", beginStream, Instant.now())
									.withMetadata("chatId", id.toString())
									.markAsSucceeded()
									.build());
					streamConfig.messageHandler().accept(new VChatMessage(r, beginStream, false));
				},
				e -> {
					analyticsManager.addSpan(TraceSpan.builder(LlmManagerImpl.LLM_CATEGORY, "chatStream", beginStream, Instant.now())
							.withMetadata("chatId", id.toString())
							.markAsFailed(e)
							.build());
					streamConfig.errorHandler().accept(e);
				},
				streamConfig.throttleMs()));
	}

	protected abstract VLlmMessage doChat(final String instructions);

	protected abstract void doChatStream(final String instructions, VLlmMessageStreamConfig<VLlmMessage> streamConfig);

}
