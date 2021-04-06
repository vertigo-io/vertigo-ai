package io.vertigo.ai.impl.bot;

import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.ai.bb.BlackBoard;
import io.vertigo.ai.bb.BlackBoardManager;
import io.vertigo.ai.bot.BotEngine;
import io.vertigo.ai.bot.BotManager;
import io.vertigo.ai.bot.BotResponse;
import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BTStatus;
import io.vertigo.ai.bt.BehaviorTreeManager;
import io.vertigo.core.lang.Assertion;

public class BotManagerImpl implements BotManager {
	private final BlackBoardManager blackBoardManager;
	private final BehaviorTreeManager behaviorTreeManager;

	@Inject
	public BotManagerImpl(
			final BlackBoardManager blackBoardManager,
			final BehaviorTreeManager behaviorTreeManager) {
		Assertion.check()
				.isNotNull(blackBoardManager)
				.isNotNull(behaviorTreeManager);
		//---
		this.blackBoardManager = blackBoardManager;
		this.behaviorTreeManager = behaviorTreeManager;
	}

	@Override
	public BotEngine createBotEngine(final String storeName) {
		return new BotEngine(blackBoardManager.connect(storeName));
	}

	@Override
	public BotResponse runTick(final BTNode bot, final String storeName, final Optional<String> userResponseOpt) {
		final BlackBoard blackBoard = blackBoardManager.connect(storeName);
		userResponseOpt.ifPresent(response -> {
			final var key = blackBoard.getString("bot/response");
			final var type = blackBoard.getString("bot/response/type");
			if ("integer".equals(type)) {
				blackBoard.putInteger(key, Integer.valueOf(response));
			} else {
				blackBoard.putString(key, response);
			}
		});
		if (behaviorTreeManager.run(bot) == BTStatus.Running) {
			return BotResponse.talk(blackBoard.getString("bot/question"));
		}
		return BotResponse.BOT_RESPONSE_END;
	}
}
