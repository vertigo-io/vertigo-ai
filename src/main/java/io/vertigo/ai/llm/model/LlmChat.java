package io.vertigo.ai.llm.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.vertigo.ai.impl.llm.LlmManagerImpl;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.trace.TraceSpan;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * A chat session.
 */
public abstract class LlmChat {

	protected final UUID id;
	protected Instant lastUse;
	protected final List<VChatMessage> messages;
	protected final List<VFile> files;
	protected final VPromptContext context;

	private final AnalyticsManager analyticsManager;

	protected LlmChat(final List<VFile> files) {
		this(files, null);
	}

	protected LlmChat(final List<VFile> files, final VPromptContext context) {
		id = UUID.randomUUID();
		lastUse = Instant.now();
		messages = new ArrayList<>();
		this.files = files;
		this.context = context == null ? new VPromptContext() : context;

		analyticsManager = Node.getNode().getComponentSpace().resolve(AnalyticsManager.class);
	}

	/**
	 * @return the chat id
	 */
	public final UUID getId() {
		return id;
	}

	/**
	 * @return the last use date
	 */
	public final Instant getLastUse() {
		return lastUse;
	}

	/**
	 * @return the files used as sources
	 */
	public final List<VFile> getFiles() {
		return files;
	}

	/**
	 * @return the full conversation
	 */
	public final List<VChatMessage> getMessages() {
		return messages;
	}

	/**
	 * @return the context
	 */
	public final VPromptContext getContext() {
		return context;
	}

	/**
	 * Chat with the assistant.
	 *
	 * @param instructions the instructions
	 * @return the chat message
	 */
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
