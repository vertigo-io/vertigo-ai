package io.vertigo.ai.bb.memory;

import io.vertigo.ai.AiFeatures;
import io.vertigo.ai.bb.AbstractBBBlackBoardTest;
import io.vertigo.core.node.config.NodeConfig;

public class MemoryBBBlackBoardTest extends AbstractBBBlackBoardTest {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(
						new AiFeatures()
								.withBlackboard()
								.withMemoryBlackboard()
								.build())
				.build();
	}

}
