package io.vertigo.ai.llm.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.vertigo.datastore.filestore.model.VFile;

public abstract class LlmChat {

	protected final Long id;
	protected Instant lastUse;
	protected final List<VChatMessage> messages;
	protected final List<VFile> files;

	protected LlmChat(final Long id, final List<VFile> files) {
		this.id = id;
		lastUse = Instant.now();
		messages = new ArrayList<>();
		this.files = files;
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

	public final VLlmResult chat(final VPrompt prompt) {
		lastUse = Instant.now();
		return doChat(prompt);
	}

	protected abstract VLlmResult doChat(final VPrompt prompt);

}
