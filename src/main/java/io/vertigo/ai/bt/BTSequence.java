package io.vertigo.ai.bt;

import java.util.List;

import io.vertigo.core.lang.Assertion;

/**
 * One of the two most important composite node with the selector.
 * 
 * A sequence is composed of many nodes.
 * It succeeds when all the nodes succeed.
 * It fails when one node fails.
 * After this failure the other nodes are not evaluated.
 * 
 * @author pchretien
 */
final class BTSequence implements BTNode {
	private final List<BTNode> nodes;

	BTSequence(final List<? extends BTNode> nodes) {
		Assertion.check()
				.isNotNull(nodes);
		//---
		this.nodes = List.copyOf(nodes);
	}

	@Override
	public BTStatus eval() {
		for (final BTNode node : nodes) {
			final var status = node.eval();
			//continue when succeeded until a failure or a running task
			if (!status.isSucceeded()) {
				return status;
			}
		}
		return BTStatus.Succeeded;
	}
}
