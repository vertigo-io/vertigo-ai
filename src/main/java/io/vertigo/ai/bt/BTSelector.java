package io.vertigo.ai.bt;

import java.util.List;

import io.vertigo.core.lang.Assertion;

/**
 * One of the two most important composite node with the sequence.
 * 
 * A selector is composed of many nodes.
 * It succeeds when only one node succeeds.
 * After this success the other nodes are not evaluated.
 * 
 * A selector fails when all the nodes fail.
 * 
 * @author pchretien
 */
final class BTSelector implements BTNode {
	private final List<BTNode> nodes;

	BTSelector(final List<? extends BTNode> nodes) {
		Assertion.check()
				.isNotNull(nodes);
		//---
		this.nodes = List.copyOf(nodes);
	}

	@Override
	public BTStatus eval() {
		for (final BTNode node : nodes) {
			final var status = node.eval();
			//continue on failure until success or a running task
			if (!status.isFailed()) {
				return status;
			}
		}
		return BTStatus.Failed;
	}
}
