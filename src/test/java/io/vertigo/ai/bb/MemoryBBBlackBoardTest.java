package io.vertigo.ai.bb;

import io.vertigo.ai.AiFeatures;
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
