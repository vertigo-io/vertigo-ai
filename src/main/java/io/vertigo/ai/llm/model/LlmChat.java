package io.vertigo.ai.llm.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.vertigo.ai.impl.llm.LlmManagerImpl;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.node.Node;
import io.vertigo.datastore.filestore.model.VFile;

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

	public final Long getId() {
		return id;
	}

	public final Instant getLastUse() {
		return lastUse;
	}

	public final List<VFile> getFiles() {
		return files;
	}

	public final List<VChatMessage> getMessages() {
		return messages;
	}

	public final VPromptContext getContext() {
		return context;
	}

	public final VLlmResult chat(final String instructions) {
		return analyticsManager.traceWithReturn(LlmManagerImpl.LLM_CATEGORY, "chat", tracer -> {
			tracer.setMetadata("chatId", id.toString());

			lastUse = Instant.now();
			return doChat(instructions);
		});
	}

	protected abstract VLlmResult doChat(final String instructions);

}
