package io.vertigo.ai.bb;

import io.vertigo.core.node.component.Manager;

/**
 * This manager provides blackboards. 
 * You have to connect to a blackboard by its name.
 * 
 * You can copy a tree of keys (with values) from a blackboard to another.
 * 
 * @author pchretien
 */
public interface BlackBoardManager extends Manager {
	String STORE_NAME_REGEX = "[a-z]+";

	String MAIN_STORE_NAME = "main";

	/**
	 * Connects to a blackboard identified by its name.
	 * @param storeName the name of the blackboard
	 * @return the blackboard
	 */
	BlackBoard connect(String storeName);

	/**
	 * Connects to the main blackboard
	 * @return the main blackboard
	 */
	default BlackBoard connect() {
		return connect(MAIN_STORE_NAME);
	}
}
