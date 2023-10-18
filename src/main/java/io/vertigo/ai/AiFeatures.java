package io.vertigo.ai;

import io.vertigo.ai.bt.BehaviorTreeManager;
import io.vertigo.ai.command.BtCommandManager;
import io.vertigo.ai.impl.bt.BehaviorTreeManagerImpl;
import io.vertigo.ai.impl.command.BtCommandManagerImpl;
import io.vertigo.ai.impl.nlu.NluManagerImpl;
import io.vertigo.ai.nlu.NluManager;
import io.vertigo.ai.plugins.nlu.rasa.RasaNluEnginePlugin;
import io.vertigo.core.node.config.Feature;
import io.vertigo.core.node.config.Features;
import io.vertigo.core.param.Param;

public class AiFeatures extends Features<AiFeatures> {

	/**
	 * Constructor.
	 */
	public AiFeatures() {
		super("vertigo-ai");
	}

	/**
	 * Activates Behavior Tree.
	 *
	 * @return these features
	 */
	@Feature("parser")
	public AiFeatures withParser() {
		getModuleConfigBuilder()
				.addComponent(BtCommandManager.class, BtCommandManagerImpl.class);
		return this;
	}

	/**
	 * Activates NLU.
	 *
	 * @return these features
	 */
	@Feature("nlu")
	public AiFeatures withNLU() {
		getModuleConfigBuilder()
				.addComponent(NluManager.class, NluManagerImpl.class);
		return this;
	}

	/**
	 * Activates NLU.
	 *
	 * @return these features
	 */
	@Feature("nlu.rasa")
	public AiFeatures withRasaNLU(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(RasaNluEnginePlugin.class, params);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(BehaviorTreeManager.class, BehaviorTreeManagerImpl.class); // no params or plugin so always here!
		//
	}
}
