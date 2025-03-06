package io.vertigo.ai.llm.model.rag;

import java.util.List;
import java.util.Map;

import io.vertigo.datastore.filestore.model.FileInfoURI;

public interface VLlmDocumentSource {
	public static final String FILE_URN_METADATA = "file_urn";

	void addDocument(VLlmDocument document);

	void removeDocument(FileInfoURI fileInfoURI);

	/**
	 * Search for documents filtered by metadata.
	 *
	 * @param query the query in natural language
	 * @param metadataFilter filter on metadata values
	 * @param maxResults the maximum number of results
	 * @param minScore the minimum score
	 * @return the documents
	 */
	List<VLlmDocumentSearchResult> search(String query, Map<String, Object> metadataFilter, final Integer maxResults, final Double minScore);

	/**
	 * Search for documents. It returns 10 documents maximum with a minimum score of 0.5.
	 *
	 * @param query the query in natural language
	 * @return the documents
	 */
	default List<VLlmDocumentSearchResult> search(final String query) {
		return search(query, Map.of(), 10, 0.6d);
	}

	/**
	 * Search for documents filtered by metadata. It returns 10 documents maximum with a minimum score of 0.5.
	 *
	 * @param query the query in natural language
	 * @param metadataFilter filter on metadata values
	 * @return the documents
	 */
	default List<VLlmDocumentSearchResult> search(final String query, final Map<String, Object> metadataFilter) {
		return search(query, metadataFilter, 10, 0.6d);
	}

	boolean isEmpty();

}
