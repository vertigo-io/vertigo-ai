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
	 * Starts the process to train the neural network with registered intents.
	 */
	void train(final Map<NluIntent, List<String>> trainingData);

	/**
	 * Recognizes intents from a sentence.
	 * Uses the previously trained model to recognize a new and unknown sentence.
	 *
	 * @param sentence the sentence we want to recognize.
	 * @return the result of the recognition
	 */
	NluResult recognize(String sentence);

	/**
	 * Checks if ready to recognize sentences.
	 *
	 * @return true if the engine is ready
	 */
	boolean isReady();

	/**
	 * Checks if Nlu engine is alive
	 *
	 * @return true if the engine is alive
	 */
	boolean isAlive();

	/**
	 * Returns the Plugin name. Default to "main".
	 *
	 * @return the name of the plugin
	 */
	String getName();
}
