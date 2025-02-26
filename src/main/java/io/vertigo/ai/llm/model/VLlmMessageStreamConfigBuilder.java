package io.vertigo.ai.llm.model;

import java.util.function.Consumer;

import io.vertigo.core.lang.Builder;

public final class VLlmMessageStreamConfigBuilder<R> implements Builder<VLlmMessageStreamConfig<R>> {

	private static <T> Consumer<T> noop(final T r) {
		return t -> {
			// NOOP
		};
	};

	private Consumer<String> tokenHandler = VLlmMessageStreamConfigBuilder::noop;
	private Consumer<R> partialMessageHandler = VLlmMessageStreamConfigBuilder::noop;
	private Consumer<R> messageHandler = VLlmMessageStreamConfigBuilder::noop;
	private Consumer<Throwable> errorHandler = VLlmMessageStreamConfigBuilder::noop;

	public VLlmMessageStreamConfigBuilder<R> withTokenHandler(final Consumer<String> newTokenHandler) {
		tokenHandler = newTokenHandler;
		return this;
	}

	public VLlmMessageStreamConfigBuilder<R> withPartialMessageHandler(final Consumer<R> newPartialMessageHandler) {
		partialMessageHandler = newPartialMessageHandler;
		return this;
	}

	public VLlmMessageStreamConfigBuilder<R> withMessageHandler(final Consumer<R> newMessageHandler) {
		messageHandler = newMessageHandler;
		return this;
	}

	public VLlmMessageStreamConfigBuilder<R> withErrorHandler(final Consumer<Throwable> newErrorHandler) {
		errorHandler = newErrorHandler;
		return this;
	}

	@Override
	public VLlmMessageStreamConfig<R> build() {
		return new VLlmMessageStreamConfig<>(tokenHandler, partialMessageHandler, messageHandler, errorHandler);
	}

}
