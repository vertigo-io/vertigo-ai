package io.vertigo.ai.bt;

import io.vertigo.core.node.component.Manager;

/**
 * Behavior Trees (or BT) are composed of nodes. 
 * This composition brings modularity.
 * 
 * A the top of the tree there is ONE single node.
 * To run the BT, you have to run this top node.
 * 
 * @author pchretien
 */
public interface BehaviorTreeManager extends Manager {

	/**
	 * Runs a BT.
	 * @param rootNode the root node 
	 * @return the status
	 */
	BTStatus run(BTNode rootNode);

}
