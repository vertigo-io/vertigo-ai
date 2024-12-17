package io.vertigo.ai.impl.nlu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.ai.nlu.NluIntent;
import io.vertigo.ai.nlu.NluManager;
import io.vertigo.ai.nlu.NluResult;
import io.vertigo.core.lang.Assertion;

/**
 * @author skerdudou
 */
public class NluManagerImpl implements NluManager {
	private final Map<String, NluEnginePlugin> nluEnginePluginMap;

	/**
	 * Constructor.
	 *
	 * @param nluEnginePlugins List of NLU engine plugins
	 */
	@Inject
	public NluManagerImpl(final List<NluEnginePlugin> nluEnginePlugins) {
		Assertion.check().isNotNull(nluEnginePlugins);
		//---
		nluEnginePluginMap = new HashMap<>();

		for (final NluEnginePlugin nluEnginePlugin : nluEnginePlugins) {
			final String name = nluEnginePlugin.getName();
			final NluEnginePlugin previous = nluEnginePluginMap.put(name, nluEnginePlugin);
			Assertion.check().isNull(previous, "NluEnginePlugin {0}, was already registered", name);
		}
	}

	private NluEnginePlugin getEngineByName(final String name) {
		Assertion.check().isNotBlank(name);
		//---
		final NluEnginePlugin nluEnginePlugin = nluEnginePluginMap.get(name);
		Assertion.check().isNotNull(nluEnginePlugin, "NluEnginePlugin {0}, wasn't registered.", name);
		return nluEnginePlugin;
	}

	@Override
	public void train(final Map<NluIntent, List<String>> trainingData, final String engineName) {
		Assertion.check()
				.isNotBlank(engineName);
		//---
		getEngineByName(engineName)
				.train(trainingData);

	}

	@Override
	public NluResult recognize(final String sentence, final String engineName) {
		return getEngineByName(engineName).recognize(sentence);
	}

	@Override
	public boolean isReady(final String engineName) {
		return getEngineByName(engineName).isReady();
	}

	@Override
	public boolean isAlive(final String engineName) {
		return getEngineByName(engineName).isAlive();
	}
}
