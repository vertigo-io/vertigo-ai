package io.vertigo.ai.bt;

import io.vertigo.core.lang.Assertion;

/**
 * The root of an effective tree. 
 * 
 * @author pchretien
 */
public final class BTRoot {
	private final BTNode rootNode;

	public BTRoot(final BTNode rootNode) {
		Assertion.check()
				.isNotNull(rootNode);
		//---
		this.rootNode = rootNode;
	}

	public BTStatus run() {
		return rootNode.eval();
	}
}
