package io.vertigo.ai.llm.model;

import java.time.Instant;

public record VChatMessage(VLlmMessage message, Instant date, boolean fromUser) {
}
