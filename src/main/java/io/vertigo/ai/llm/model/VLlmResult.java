package io.vertigo.ai.llm.model;

import java.util.List;

public interface VLlmResult {

	String getText();

	String getMarkdown();

	String getHtml();

	List<String> getSources(); // TODO : improve this later

}
