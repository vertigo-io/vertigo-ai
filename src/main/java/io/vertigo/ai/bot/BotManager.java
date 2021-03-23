package io.vertigo.ai.bot;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.core.node.component.Component;

public interface BotManager extends Component {

	BotEngine createBotEngine(String storeName);

	void runInConsole(final BTNode bot, final String storeName);

}
