package io.vertigo.ai.impl.nlu;

import java.util.List;
import java.util.Map;

import io.vertigo.ai.nlu.Intent;
import io.vertigo.ai.nlu.RecognitionResult;
import io.vertigo.core.node.component.Plugin;

/**
 * @author skerdudou
 */
public interface NluEnginePlugin extends Plugin {

	/**
	 * Starts the process to train the neural network against registered intents.
	 */
	void train(final Map<Intent, List<String>> trainingData);

	/**
	 * Uses the previously trained model to classify a new and unknown sentence.
	 *
	 * @param sentence the sentence we wants to classify.
	 * @return the result of the analysis
	 */
	RecognitionResult recognize(String sentence);

	/**
	 * Checks if ready to recognize sentences.
	 *
	 * @return true if the engine is ready
	 */
	boolean isReady();

	/**
	 * Plugin name. Default to "main".
	 *
	 * @return the name of the plugin
	 */
	String getName();
}
