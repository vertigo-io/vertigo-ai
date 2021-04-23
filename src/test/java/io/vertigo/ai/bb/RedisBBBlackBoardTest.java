package io.vertigo.ai.bb;

import io.vertigo.ai.AiFeatures;
import io.vertigo.connectors.redis.RedisFeatures;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;

public class RedisBBBlackBoardTest extends AbstractBBBlackBoardTest {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(new RedisFeatures()
						.withJedis(
								Param.of("host", "localhost"),
								Param.of("port", 6379),
								Param.of("database", 0))
						.build())
				.addModule(
						new AiFeatures()
								.withBlackboard()
								.withRedisBlackboard()
								.build())
				.build();
	}

}
