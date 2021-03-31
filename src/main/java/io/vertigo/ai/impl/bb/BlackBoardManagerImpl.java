package io.vertigo.ai.impl.bb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.ai.bb.BlackBoard;
import io.vertigo.ai.bb.BlackBoardManager;
import io.vertigo.core.lang.Assertion;

public final class BlackBoardManagerImpl implements BlackBoardManager {
	private final Map<String, BlackBoardStorePlugin> blackBoardPluginByStore = new HashMap<>();

	@Inject
	public BlackBoardManagerImpl(
			final List<BlackBoardStorePlugin> blackBoardStorePlugins) {
		Assertion.check()
				.isNotNull(blackBoardStorePlugins);
		// ---
		blackBoardStorePlugins.forEach(
				plugin -> {
					final var storeName = plugin.getStoreName();
					Assertion.check()
							.isNotBlank(storeName)
							.isFalse(blackBoardPluginByStore.containsKey(storeName), "BlackBoard Store '{0}' already registered ", storeName)
							.isTrue(storeName.matches(STORE_NAME_REGEX), "the storename '{0}' must contain only a-z words", storeName);
					//---
					blackBoardPluginByStore.put(storeName, plugin);
				});
	}

	@Override
	public BlackBoard connect(final String storeName) {
		Assertion.check()
				.isNotBlank(storeName, "A storeName is mandatory to connect with a blackboard");
		//---
		return new BlackBoardImpl(getPlugin(storeName));
	}

	//------------------------------------
	//- Utils                             -
	//------------------------------------

	private BlackBoardStorePlugin getPlugin(final String storeName) {
		Assertion.check()
				.isTrue(blackBoardPluginByStore.containsKey(storeName), " Store with name '{0}' doesn't exist", storeName);
		//---
		return blackBoardPluginByStore.get(storeName);
	}
}
