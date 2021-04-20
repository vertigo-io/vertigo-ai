package io.vertigo.ai.bt.command;

import java.util.List;
import java.util.function.Function;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.core.node.component.Manager;

/**
 * Command manager transforms "commands" as diverse input form into a BT.
 * Commands are by nature extensible by plugins and can require additional parameters.
 * Theses parameters are passed via the returned function that produces a BT.
 *
 * @author skerdudou
 */
public interface BtCommandManager extends Manager {

	/**
	 * Transform commands as input text.
	 *
	 * @param text input commands
	 * @return the function to produce the BT
	 */
	Function<List<Object>, BTNode> parse(String text);

}