package io.vertigo.ai.bt;

import io.vertigo.core.node.component.Manager;

public interface BehaviorTreeManager extends Manager {

	BTStatus run(BTNode rootNode);

}
