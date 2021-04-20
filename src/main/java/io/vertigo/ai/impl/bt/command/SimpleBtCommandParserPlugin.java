package io.vertigo.ai.impl.bt.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;

/**
 * Class pattern for a ParserPlugin with a simple command registration.
 *
 * @author skerdudou
 * @param <P> Type of the BtNodeProvider
 */
public abstract class SimpleBtCommandParserPlugin implements BtCommandParserPlugin {

	private final Map<String, CommandResolver> basicCommands = new HashMap<>();
	private final Map<String, CommandResolver> compositeCommands = new HashMap<>();

	public SimpleBtCommandParserPlugin() {
		init();
	}

	/**
	 * Register commands with either registerBasicCommand, registerCompositeCommand or registerCommands.
	 */
	protected abstract void init(); // prevent empty concrete classes

	@Override
	public final Optional<Function<List<Object>, BTNode>> parse(final BtCommand command, final List<BTNode> childs) {
		Assertion.check()
				.isNotNull(command)
				.isNotNull(childs);
		//--
		switch (command.getType()) {
			case STANDARD:
				Assertion.check()
						.isTrue(childs.isEmpty(), "Standard commands dont expect childs");
				// --
				return Optional.ofNullable(basicCommands.get(command.getCommandName()))
						.map(resolver -> p -> resolver.apply(command, p, Collections.emptyList()));
			case START_COMPOSITE:
				return Optional.ofNullable(compositeCommands.get(command.getCommandName()))
						.map(resolver -> p -> resolver.apply(command, p, childs));
			default:
				throw new VSystemException("Parser plugin don't handle {0} command types", command.getType());
		}
	}

	/**
	 * Register a function to convert a basic command to a node provider.
	 *
	 * @param name the name of the command
	 * @param commandConverter the function to convert a command and a nodeProvider into a node
	 */
	protected final void registerBasicCommand(final String name, final BiFunction<BtCommand, List<Object>, BTNode> commandConverter) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(commandConverter);
		//--
		basicCommands.put(name, (c, p, l) -> commandConverter.apply(c, p));
	}

	/**
	 * Register a function to convert a composite command to a node provider.
	 *
	 * @param name the name of the command
	 * @param commandConverter the function to convert a command, a nodeProvider and a list of child nodes into a node
	 */
	protected final void registerCompositeCommand(final String name, final CommandResolver commandConverter) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(commandConverter);
		//--
		compositeCommands.put(name, commandConverter);
	}

	/**
	 * Register a function to convert a basic command to a node.
	 * Here stateless means, no need for BtNodeProvider object.
	 *
	 * @param name the name of the command
	 * @param commandConverter function to convert a command into a node
	 */
	protected final void registerStatelessBasicCommand(final String name, final Function<BtCommand, BTNode> commandConverter) {
		registerBasicCommand(name, (c, p) -> commandConverter.apply(c));
	}

	/**
	 * Register a function to convert a composite command to a node .
	 * Here stateless means, no need for BtNodeProvider object.
	 *
	 * @param name the name of the command
	 * @param commandConverter function to convert a command and a list of child nodes into a node
	 */
	protected final void registerStatelessCompositeCommand(final String name, final BiFunction<BtCommand, List<BTNode>, BTNode> commandConverter) {
		registerCompositeCommand(name, (c, p, l) -> commandConverter.apply(c, l));
	}

	/**
	 * A TriFunction that resolves inputs (Command, node provider and childs) into a BTNode.
	 *
	 * @author skerdudou
	 * @param <P> Type of the BtNodeProvider
	 */
	@FunctionalInterface
	public interface CommandResolver {
		BTNode apply(BtCommand c, List<Object> p, List<BTNode> l);
	}
}
