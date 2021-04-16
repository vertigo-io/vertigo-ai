package io.vertigo.ai.bt.parser;

import java.util.List;
import java.util.function.Function;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.core.node.component.Manager;

public interface BtCommandManager extends Manager {

	Function<List<Object>, BTNode> parse(String text);

}
