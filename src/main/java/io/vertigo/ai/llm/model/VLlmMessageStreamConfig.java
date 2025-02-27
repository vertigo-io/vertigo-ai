package io.vertigo.ai.llm.model;

import java.util.function.Consumer;

public record VLlmMessageStreamConfig<R>(
		Consumer<String> tokenHandler,
		Consumer<R> partialMessageHandler,
		Consumer<R> messageHandler,
		Consumer<Throwable> errorHandler,
		int throttleMs) {

	public static VLlmMessageStreamConfigBuilder<VChatMessage> builder() {
		return new VLlmMessageStreamConfigBuilder<>();
	}
}
