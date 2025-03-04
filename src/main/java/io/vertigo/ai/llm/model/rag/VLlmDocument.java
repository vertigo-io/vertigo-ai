package io.vertigo.ai.llm.model.rag;

import java.util.HashMap;
import java.util.Map;

import io.vertigo.datastore.filestore.model.FileInfo;

public record VLlmDocument(FileInfo fileInfo, Map<String, Object> metadatas) {

	public VLlmDocument(final FileInfo fileInfo, final Map<String, Object> metadatas) {
		this.fileInfo = fileInfo;
		this.metadatas = new HashMap<>(metadatas); // enforce mutable
	}

	public VLlmDocument(final FileInfo fileInfo) {
		this(fileInfo, Map.of());
	}

}
