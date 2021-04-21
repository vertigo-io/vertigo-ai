package io.vertigo.ai.impl.nlu;

import java.util.List;
import java.util.Map;

import io.vertigo.ai.nlu.NluIntent;
import io.vertigo.ai.nlu.NluResult;
import io.vertigo.core.node.component.Plugin;

/**
 * @author skerdudou
 */
public interface NluEnginePlugin extends Plugin {

	/**
	 * Starts the process to train the neural network against registered intents.
	 */
	void train(final Map<NluIntent, List<String>> trainingData);

	/**
	 * Uses the previously trained model to classify a new and unknown sentence.
	 *
	 * @param sentence the sentence we wants to classify.
	 * @return the result of the analysis
	 */
	NluResult recognize(String sentence);

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
