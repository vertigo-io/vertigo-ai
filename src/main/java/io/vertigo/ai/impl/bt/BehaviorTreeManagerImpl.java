package io.vertigo.ai.impl.bt;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BTStatus;
import io.vertigo.ai.bt.BehaviorTreeManager;
import io.vertigo.core.lang.Assertion;

public class BehaviorTreeManagerImpl implements BehaviorTreeManager {

	@Override
	public BTStatus run(final BTNode rootNode) {
		Assertion.check()
				.isNotNull(rootNode);
		//---
		return rootNode.eval();
	}

}
