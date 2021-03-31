package io.vertigo.ai.bb;

import io.vertigo.core.node.component.Component;

public interface BlackBoardManager extends Component {
	String STORE_NAME_REGEX = "[a-z]+";

	String MAIN_STORE_NAME = "main";

	BlackBoard connect(String storeName);

	default BlackBoard connect() {
		return connect(MAIN_STORE_NAME);
	}
}
