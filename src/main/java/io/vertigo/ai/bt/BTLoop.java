package io.vertigo.ai.bt;

import io.vertigo.core.lang.Assertion;

/**
 * Loop permits to repeat a node or a sequence of nodes
 *  - while a specific condition is met 
 *  - until a specific condition is met
 * 
 * @author pchretien
 */
final class BTLoop implements BTNode {
	/* This is a security to break the loop if the condition is never attempted */
	static final int MAX_LOOPS = 10_000;
	private final BTCondition whileCondition;
	private final BTCondition untilCondition;
	private final BTNode node;

	BTLoop(final int loops, final BTCondition whileCondition, final BTNode node, final BTCondition untilCondition) {
		Assertion.check()
				.isTrue(loops >= 0, "loops must be >= 0")
				.isNotNull(whileCondition)
				.isNotNull(node)
				.isNotNull(untilCondition);
		//---
		this.whileCondition = whileCondition;
		this.node = node;
		this.untilCondition = untilCondition;
	}

	@Override
	public BTStatus eval() {
		for (int i = 0; i < MAX_LOOPS; i++) {
			final var whileTest = whileCondition.eval();
			//breaks the loop when the while condition failed
			if (whileTest.isFailed()) {
				return BTStatus.Succeeded;
			}

			final var status = node.eval();
			//loops when succeeded until failure or a running task
			if (!status.isSucceeded()) {
				return status;
			}

			final var untilTest = untilCondition.eval();
			//breaks the loop when the until condition succeeded
			if (untilTest.isSucceeded()) {
				return BTStatus.Succeeded;
			}
		}
		return BTStatus.Failed;
	}

}
