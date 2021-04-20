package io.vertigo.ai.impl.bt.command;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.impl.bt.command.BtCommand.CommandType;
import io.vertigo.core.lang.Assertion;

/**
 * Plugin to parse a command into a function providing the associated BtNode. The plugin may need an intermediate object called BtNodeProvider that is resolved at runtime and
 * needed to resolve the BtNode.
 *
 * @author skerdudou
 * @param <P> Type of the BtNodeProvider
 */
public class BtCommandParser {

	private final String commandName;
	private final CommandType commandType;
	private final CommandEvaluator commandEvaluator;

	BtCommandParser(final CommandType commandType, final String commandName, final CommandEvaluator commandEvaluator) {
		this.commandType = commandType;
		this.commandName = commandName;
		this.commandEvaluator = commandEvaluator;
	}

	public CommandType getCommandType() {
		return commandType;
	}

	public String getCommandName() {
		return commandName;
	}

	public CommandEvaluator getCommandResolver() {
		return commandEvaluator;
	}

	/**
	 * Register a function to convert a basic command to a node provider.
	 *
	 * @param name the name of the command
	 * @param commandConverter the function to convert a command and a nodeProvider into a node
	 */
	public static final BtCommandParser basicCommand(final String name, final BiFunction<BtCommand, List<Object>, BTNode> commandConverter) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(commandConverter);
		//--
		return new BtCommandParser(CommandType.STANDARD, name, (c, p, l) -> commandConverter.apply(c, p));
	}

	/**
	 * Register a function to convert a composite command to a node provider.
	 *
	 * @param name the name of the command
	 * @param commandConverter the function to convert a command, a nodeProvider and a list of child nodes into a node
	 * @return
	 */
	public static final BtCommandParser compositeCommand(final String name, final CommandEvaluator commandConverter) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(commandConverter);
		//--
		return new BtCommandParser(CommandType.START_COMPOSITE, name, commandConverter);
	}

	/**
	 * Register a function to convert a basic command to a node.
	 * Here stateless means, no need for BtNodeProvider object.
	 *
	 * @param name the name of the command
	 * @param commandConverter function to convert a command into a node
	 */
	public static final BtCommandParser statelessBasicCommand(final String name, final Function<BtCommand, BTNode> commandConverter) {
		return basicCommand(name, (c, p) -> commandConverter.apply(c));
	}

	/**
	 * Register a function to convert a composite command to a node .
	 * Here stateless means, no need for BtNodeProvider object.
	 *
	 * @param name the name of the command
	 * @param commandConverter function to convert a command and a list of child nodes into a node
	 */
	public static final BtCommandParser statelessCompositeCommand(final String name, final BiFunction<BtCommand, List<BTNode>, BTNode> commandConverter) {
		return compositeCommand(name, (c, p, l) -> commandConverter.apply(c, l));
	}

	/**
	 * A TriFunction that resolves inputs (Command, node provider and childs) into a BTNode.
	 *
	 * @author skerdudou
	 * @param <P> Type of the BtNodeProvider
	 */
	@FunctionalInterface
	public interface CommandEvaluator {
		BTNode apply(BtCommand c, List<Object> p, List<BTNode> l);
	}

}
