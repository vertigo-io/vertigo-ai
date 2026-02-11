package io.vertigo.ai;

import io.vertigo.ai.bb.BlackBoardManager;
import io.vertigo.ai.bt.BehaviorTreeManager;
import io.vertigo.ai.command.BtCommandManager;
import io.vertigo.ai.impl.bb.BlackBoardManagerImpl;
import io.vertigo.ai.impl.bt.BehaviorTreeManagerImpl;
import io.vertigo.ai.impl.command.BtCommandManagerImpl;
import io.vertigo.ai.impl.llm.LlmManagerImpl;
import io.vertigo.ai.impl.nlu.NluManagerImpl;
import io.vertigo.ai.llm.LlmManager;
import io.vertigo.ai.nlu.NluManager;
import io.vertigo.ai.plugins.bb.memory.MemoryBlackBoardStorePlugin;
import io.vertigo.ai.plugins.bb.redis.RedisBlackBoardStorePlugin;
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
	 * Activates BlackBoard.
	 *
	 * @return these features
	 */
	@Feature("blackboard")
	public AiFeatures withBlackboard() {
		getModuleConfigBuilder()
				.addComponent(BlackBoardManager.class, BlackBoardManagerImpl.class);
		return this;
	}

	/**
	 * Add ability to use memory plugin to store Blackboards.
	 *
	 * @return these features
	 */
	@Feature("blackboard.memory")
	public AiFeatures withMemoryBlackboard(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(MemoryBlackBoardStorePlugin.class, params);
		return this;
	}

	/**
	 * Add ability to use redis plugin to store Blackboards.
	 *
	 * @return these features
	 */
	@Feature("blackboard.redis")
	public AiFeatures withRedisBlackboard(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(RedisBlackBoardStorePlugin.class, params);
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

	/**
	 * Activates LLM.
	 *
	 * @return these features
	 */
	@Feature("llm")
	public AiFeatures withLlm() {
		getModuleConfigBuilder()
				.addComponent(LlmManager.class, LlmManagerImpl.class);
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
