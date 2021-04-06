package io.vertigo.ai.bot;

import java.util.Optional;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.core.node.component.Manager;

public interface BotManager extends Manager {

	BotEngine createBotEngine(String storeName);

	BotResponse runTick(BTNode bot, String storeName, Optional<String> userResponseOpt);

}
