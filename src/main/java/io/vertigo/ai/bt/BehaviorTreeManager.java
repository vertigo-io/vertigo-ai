package io.vertigo.ai.bt;

import io.vertigo.core.node.component.Component;

public interface BehaviorTreeManager extends Component {

	BTStatus run(BTNode rootNode);

}
