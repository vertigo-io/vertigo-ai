package io.vertigo.ai.llm;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.vertigo.ai.llm.model.VChatMessage;
import io.vertigo.ai.llm.model.VLlmMessageStreamConfig;
import io.vertigo.ai.llm.model.VPromptContext;
import io.vertigo.ai.llm.model.rag.VLlmDocumentSource;

public interface LlmChat {

	/**
	 * @return the chat id
	 */
	UUID getId();

	/**
	 * @return the last use date
	 */
	Instant getLastUse();

	/**
	 * @return the full conversation
	 */
	List<VChatMessage> getMessages();

	/**
	 * @return the context
	 */
	VPromptContext getContext();

	/**
	 * @return the documentSource
	 */
	VLlmDocumentSource getDocumentSource();

	/**
	 * Chat with the assistant.
	 *
	 * @param instructions the instructions
	 * @return the chat message
	 */
	VChatMessage chat(String instructions);

	/**
	 * Chat with the assistant, streaming the results.
	 *
	 * @param instructions the instructions
	 * @param streamConfig the stream configuration, with handlers for new tokens, execution end and errors
	 */
	void chatStream(String instructions, VLlmMessageStreamConfig<VChatMessage> streamConfig);

}
