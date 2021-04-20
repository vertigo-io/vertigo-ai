package io.vertigo.ai.impl.bt.command;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BtNodeProvider;
import io.vertigo.core.node.component.Plugin;

/**
 * Plugin to parse a command into a function providing the associated BtNode. The plugin may need an intermediate object called BtNodeProvider that is resolved at runtime and
 * needed to resolve the BtNode.
 *
 * @author skerdudou
 * @param <P> Type of the BtNodeProvider
 */
public interface BtCommandParserPlugin<P extends BtNodeProvider> extends Plugin {

	/**
	 * Resolve the BtNodeProvider of the plugin. If the plugin needs a parameter (eg a BlackBoard) it will be provided in the params list.
	 *
	 * @param params parameters, if needed
	 * @return the BtNodeProvider that will be passed back when parsing
	 */
	P getNodeProvider(List<Object> params);

	/**
	 * Parses a BtCommand into a function that uses the BtNodeProvider to produce the BTNode.
	 * If the plugin don't handle this command, return an empty option.
	 *
	 * @param command the command to parse
	 * @param childs the childs in case of a composite command
	 * @return optionally, the function
	 */
	Optional<Function<P, BTNode>> parse(BtCommand command, List<BTNode> childs);
}
