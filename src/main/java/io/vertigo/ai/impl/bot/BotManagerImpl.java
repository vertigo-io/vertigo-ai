package io.vertigo.ai.impl.bot;

import java.util.Scanner;

import javax.inject.Inject;

import io.vertigo.ai.bb.BlackBoardManager;
import io.vertigo.ai.bot.BotEngine;
import io.vertigo.ai.bot.BotManager;
import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BTStatus;
import io.vertigo.ai.bt.BehaviorTreeManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.lang.Assertion;

public class BotManagerImpl implements BotManager {

	private final VTransactionManager transactionManager;
	private final BlackBoardManager blackBoardManager;
	private final BehaviorTreeManager behaviorTreeManager;

	@Inject
	public BotManagerImpl(
			final VTransactionManager transactionManager,
			final BlackBoardManager blackBoardManager,
			final BehaviorTreeManager behaviorTreeManager) {
		Assertion.check()
				.isNotNull(transactionManager)
				.isNotNull(blackBoardManager)
				.isNotNull(behaviorTreeManager);
		//---
		this.transactionManager = transactionManager;
		this.blackBoardManager = blackBoardManager;
		this.behaviorTreeManager = behaviorTreeManager;
	}

	@Override
	public BotEngine createBotEngine(final String storeName) {
		return new BotEngine(blackBoardManager, storeName);
	}

	@Override
	public void runInConsole(final BTNode bot, final String storeName) {
		Assertion.check()
				.isNotNull(bot);
		//---
		if (transactionManager.hasCurrentTransaction()) {
			// if we have a transaction we use the current one
			doRunInConsole(bot, storeName);
		}
		//else we create a new one and commit it if everything went well
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			doRunInConsole(bot, storeName);
			transaction.commit();
		}

	}

	private void doRunInConsole(final BTNode bot, final String storeName) {
		final Scanner sc = new Scanner(System.in);
		while (behaviorTreeManager.run(bot) == BTStatus.Running) {
			System.out.println(">>running *************************");
			final var response = sc.nextLine();
			final var key = blackBoardManager.format(storeName, "{{bot/response}}");
			blackBoardManager.put(storeName, key, response);
		}
		sc.close();
		System.out.println(">> end ***********************");
	}

}
