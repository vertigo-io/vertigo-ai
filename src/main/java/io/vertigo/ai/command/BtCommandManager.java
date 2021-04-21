package io.vertigo.ai.command;

import java.util.List;
import java.util.function.Function;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.core.node.component.Manager;

/**
 * Command manager transforms "commands" as diverse input form into a BT.
 * 
 * Commands are by nature extensible by Defnitions and can require additional parameters.
 * Theses parameters are passed to the returned function that produces a BT.
 *
 * @author skerdudou
 */
public interface BtCommandManager extends Manager {

	/**
	 * Parses a command as input text into a BTNode.
	 *
	 * @param text input commands
	 * @return the function to produce the BT
	 */
	Function<List<Object>, BTNode> parse(String text);
}
