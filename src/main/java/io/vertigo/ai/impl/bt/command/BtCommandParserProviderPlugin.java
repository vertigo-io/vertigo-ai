package io.vertigo.ai.impl.bt.command;

import java.util.List;
import java.util.function.Supplier;

import io.vertigo.core.node.component.Plugin;

/**
 * Plugin to parse a command into a function providing the associated BtNode. The plugin may need an intermediate object called BtNodeProvider that is resolved at runtime and
 * needed to resolve the BtNode.
 *
 * @author skerdudou
 * @param <P> Type of the BtNodeProvider
 */
public interface BtCommandParserProviderPlugin extends Plugin, Supplier<List<BtCommandParser>> {
	//

}
