package io.vertigo.ai.llm.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.vertigo.datastore.filestore.model.VFile;

public abstract class LlmChat {
	private static final Random RANDOM = new Random();

	protected final Long id;
	protected Instant lastUse;
	protected final List<VChatMessage> messages;
	protected final List<VFile> files;
	protected final VPromptContext context;

	protected LlmChat(final List<VFile> files) {
		this(files, null);
	}

	protected LlmChat(final List<VFile> files, final VPromptContext context) {
		id = RANDOM.nextLong(); // To improve security, we can add the sessionId to the key if present
		lastUse = Instant.now();
		messages = new ArrayList<>();
		this.files = files;
		this.context = context == null ? new VPromptContext() : context;
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
		lastUse = Instant.now();
		return doChat(instructions);
	}

	protected abstract VLlmResult doChat(final String instructions);

}
