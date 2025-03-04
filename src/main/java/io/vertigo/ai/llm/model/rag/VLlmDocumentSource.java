package io.vertigo.ai.llm.model.rag;

import java.util.List;

import io.vertigo.datastore.filestore.model.FileInfoURI;

public interface VLlmDocumentSource {

	void addDocument(VLlmDocument document);

	void removeDocument(FileInfoURI fileInfoURI);

	/**
	 * Search for documents.
	 *
	 * @param search the search in natural language
	 * @param maxResults the maximum number of results
	 * @param minScore the minimum score
	 * @return the documents
	 */
	List<VLlmDocumentSearchResult> search(String search, final Integer maxResults, final Double minScore);

	/**
	 * Search for documents. It returns 10 documents maximum with a minimum score of 0.5.
	 *
	 * @param search the search in natural language
	 * @return the documents
	 */
	default List<VLlmDocumentSearchResult> search(final String search) {
		return search(search, 10, 0.5d);
	}

	boolean isEmpty();

}
