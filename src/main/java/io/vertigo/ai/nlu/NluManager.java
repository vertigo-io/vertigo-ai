package io.vertigo.ai.nlu;

import java.util.List;
import java.util.Map;

import io.vertigo.core.node.component.Manager;

/**
 * Natural Language Understanding manager.
 *
 * @author skerdudou
 */
public interface NluManager extends Manager {
	String DEFAULT_ENGINE_NAME = "main";

	/**
	 * Trains the specified engine with provided data.
	 *
	 * @param trainingData all intents with their training sentences
	 * @param engineName name of the engine
	 */
	void train(Map<NluIntent, List<String>> trainingData, String engineName);

	/**
	 * Recognizes intents from a sentence.
	 * It uses the previously trained model on the specified engine to classify a new and unknown sentence.
	 *
	 * @param sentence the sentence we wants to recognize.
	 * @param engineName name of the engine
	 * @return the result of the analysis
	 */
	NluResult recognize(String sentence, String engineName);

	/**
	 * Checks if the specified engine is ready to recognize intents.
	 *
	 * @param engineName name of the engine
	 * @return true if the engine is ready
	 */
	boolean isReady(String engineName);
}
