package io.vertigo.ai.impl.llm;

import java.util.List;

public interface VLlmResult {

	String getText();

	String getMarkdown();

	String getHtml();

	List<String> getSources(); // TODO : improve this later

}
