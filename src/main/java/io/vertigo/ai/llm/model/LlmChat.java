package io.vertigo.ai.llm.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	private static final Random RANDOM = new Random();

	protected final Long id;
	protected Instant lastUse;
	protected final List<VChatMessage> messages;
	protected final List<VFile> files;
	protected final VPromptContext context;

	private final AnalyticsManager analyticsManager;

	protected LlmChat(final List<VFile> files) {
		this(files, null);
	}

	protected LlmChat(final List<VFile> files, final VPromptContext context) {
		id = RANDOM.nextLong(); // To improve security, we can add the sessionId to the key if present
		lastUse = Instant.now();
		messages = new ArrayList<>();
		this.files = files;
		this.context = context == null ? new VPromptContext() : context;

		analyticsManager = Node.getNode().getComponentSpace().resolve(AnalyticsManager.class);
	}

	/**
	 * @return the chat id
	 */
	public final Long getId() {
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
		final long start = System.currentTimeMillis();
		final var now = Instant.now();
		lastUse = now;
		doChatStream(instructions, new VLlmMessageStreamConfig<>(
				streamConfig.tokenHandler(),
				r -> streamConfig.partialMessageHandler().accept(new VChatMessage(r, now, false)),
				r -> {
					// TODO analytics not working
					analyticsManager.addSpan(TraceSpan.builder(LlmManagerImpl.LLM_CATEGORY, "chat", Instant.ofEpochMilli(start), Instant.now())
							.withMetadata("chatId", id.toString())
							.withMeasure("success", 100)
							.build());
					streamConfig.messageHandler().accept(new VChatMessage(r, now, false));
				},
				e -> {
					// TODO analytics not working
					analyticsManager.addSpan(TraceSpan.builder(LlmManagerImpl.LLM_CATEGORY, "chat", Instant.ofEpochMilli(start), Instant.now())
							.withMetadata("chatId", id.toString())
							.withMeasure("success", 0)
							.withTag("exception", e.getClass().getName())
							.build());
					streamConfig.errorHandler().accept(e);
				}));
	}

	protected abstract VLlmMessage doChat(final String instructions);

	protected abstract void doChatStream(final String instructions, VLlmMessageStreamConfig<VLlmMessage> streamConfig);

}
