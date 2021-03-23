package io.vertigo.ai.impl.bt;

import javax.inject.Inject;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BTStatus;
import io.vertigo.ai.bt.BehaviorTreeManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.lang.Assertion;

public class BehaviorTreeManagerImpl implements BehaviorTreeManager {

	private final VTransactionManager transactionManager;

	@Inject
	public BehaviorTreeManagerImpl(
			final VTransactionManager transactionManager) {
		Assertion.check()
				.isNotNull(transactionManager);
		// ---
		this.transactionManager = transactionManager;
	}

	@Override
	public BTStatus run(final BTNode rootNode) {
		Assertion.check()
				.isNotNull(rootNode);
		//---
		if (transactionManager.hasCurrentTransaction()) {
			// if we have a transaction we use the current one
			return rootNode.eval();
		}
		//else we create a new one and commit it if everything went well
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final BTStatus returnStatus = rootNode.eval();
			transaction.commit();
			return returnStatus;
		}
	}

}
