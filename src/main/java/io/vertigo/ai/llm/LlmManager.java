package io.vertigo.ai.llm;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import io.vertigo.ai.llm.model.VLlmMessage;
import io.vertigo.ai.llm.model.VPrompt;
import io.vertigo.ai.llm.model.VPromptContext;
import io.vertigo.ai.llm.model.rag.VLlmDocumentSource;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Manager;
import io.vertigo.datastore.filestore.model.FileInfoURI;

/**
 * Manager for Large Language Models usage.
 *
 * @author skerdudou
 */
public interface LlmManager extends Manager {

	/**
	 * Get the persisted document source.
	 *
	 * @return the document source
	 */
	VLlmDocumentSource getPersistedDocumentSource();

	/**
	 * Get the temporary document source.
	 *
	 * @return the document source
	 */
	VLlmDocumentSource getTemporaryDocumentSource();

	/**
	 * Ask the LLM something with the knowledge of the files from the document source filtered by metadata.
	 *
	 * @param prompt the prompt to use
	 * @param documentSource the files to use
	 * @param metadataFilter filter on documents metadata
	 * @return the LLM response
	 */
	VLlmMessage askOnFiles(VPrompt prompt, VLlmDocumentSource documentSource, final Map<String, Object> metadataFilter);

	/**
	 * Ask the LLM something with the knowledge of all the files in the document source.
	 *
	 * @param prompt the prompt to use
	 * @param documentSource the files to use
	 * @return the LLM response
	 */
	default VLlmMessage askOnAllFiles(final VPrompt prompt, final VLlmDocumentSource documentSource) {
		return askOnFiles(prompt, documentSource, null);
	}

	/**
	 * Ask the LLM something with the knowledge of the specified files (previously added to the document source).
	 *
	 * @param prompt the prompt to use
	 * @param documentSource the files to use
	 * @param fileUris the files to use
	 * @return the LLM response
	 */
	default VLlmMessage askOnSpecificFiles(final VPrompt prompt, final VLlmDocumentSource documentSource, final FileInfoURI... fileUris) {
		return askOnFiles(prompt, documentSource, Map.of(VLlmDocumentSource.FILE_URN_METADATA, Arrays.asList(fileUris)));
	}

	/**
	 * Ask the LLM something.
	 *
	 * @param prompt the prompt to use
	 * @return the LLM response
	 */
	VLlmMessage ask(VPrompt prompt);

	/**
	 * Ask the LLM something.
	 *
	 * @param prompt the prompt to use
	 * @param clazz the class of the response
	 * @return the LLM response, reported in the class structure
	 */
	<T extends Object> T ask(VPrompt prompt, Class<T> clazz);

	/**
	 * Create a new chat.
	 *
	 * @param context the context to use (Persona, ...)
	 * @param documentSource the files to use in the context of the chat
	 * @param metadataFilter filter on document by metadata
	 * @return the new chat
	 */
	LlmChat initChatOnFiles(VPromptContext context, final VLlmDocumentSource documentSource, Map<String, Object> metadataFilter);

	/**
	 * Create a new chat.
	 *
	 * @param context the context to use (Persona, ...)
	 * @param documentSource the files to use in the context of the chat
	 * @return the new chat
	 */
	default LlmChat initChatOnAllFiles(final VPromptContext context, final VLlmDocumentSource documentSource) {
		return initChatOnFiles(context, documentSource, null);
	}

	/**
	 * Create a new chat.
	 *
	 * @param context the context to use (Persona, ...)
	 * @param documentSource the files to use in the context of the chat
	 * @param fileUris the files to use in the context of the chat
	 * @return the new chat
	 */
	default LlmChat initChatOnSpecificFiles(final VPromptContext context, final VLlmDocumentSource documentSource, final FileInfoURI... fileUris) {
		Assertion.check()
				.isNotNull(fileUris)
				.isTrue(fileUris.length > 0, "At least one file URI must be provided");
		return initChatOnFiles(context, documentSource, Map.of(VLlmDocumentSource.FILE_URN_METADATA, Arrays.asList(fileUris)));
	}

	/**
	 * Get a chat by its id.
	 *
	 * @param id the id of the chat
	 * @return the chat
	 */
	LlmChat getChat(final UUID id);

}
