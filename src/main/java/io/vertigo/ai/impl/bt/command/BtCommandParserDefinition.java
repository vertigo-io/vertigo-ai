package io.vertigo.ai.impl.bt.command;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.impl.bt.command.BtCommand.CommandType;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;
import io.vertigo.core.util.StringUtil;

/**
 * Plugin to parse a command (and eventually child nodes for composites) into a BtNode.
 * The plugin may need external parameters that is resolved at runtime and needed to resolve the BtNode.
 *
 * @author skerdudou
 */
@DefinitionPrefix("BtCP")
public class BtCommandParserDefinition extends AbstractDefinition {

	private final String commandName;
	private final CommandType commandType;
	private final CommandEvaluator commandEvaluator;

	BtCommandParserDefinition(final CommandType commandType, final String commandName, final CommandEvaluator commandEvaluator) {
		super("BtCP" + StringUtil.first2UpperCase(commandName) + "$h" + commandEvaluator.hashCode()); // adds the hashcode to have for now a simple mecanism to override a command parser
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
	 * Register a function to convert a basic command and his parameters into a node.
	 *
	 * @param name the name of the command
	 * @param commandConverter the function to convert a command and a nodeProvider into a node
	 * @return the BtCommandParser
	 */
	public static final BtCommandParserDefinition basicCommand(final String name, final BiFunction<BtCommand, List<Object>, BTNode> commandConverter) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(commandConverter);
		//--
		return new BtCommandParserDefinition(CommandType.STANDARD, name, (c, p, l) -> commandConverter.apply(c, p));
	}

	/**
	 * Register a function to convert a composite command with his child nodes and his parameters into a node.
	 *
	 * @param name the name of the command
	 * @param commandConverter the function to convert a command, external parameters and a list of child nodes into a node
	 * @return the BtCommandParser
	 */
	public static final BtCommandParserDefinition compositeCommand(final String name, final CommandEvaluator commandConverter) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(commandConverter);
		//--
		return new BtCommandParserDefinition(CommandType.START_COMPOSITE, name, commandConverter);
	}

	/**
	 * Register a function to convert a basic command to a node.
	 * Here stateless means, no need for external parameters.
	 *
	 * @param name the name of the command
	 * @param commandConverter function to convert a command into a node
	 * @return the BtCommandParser
	 */
	public static final BtCommandParserDefinition statelessBasicCommand(final String name, final Function<BtCommand, BTNode> commandConverter) {
		return basicCommand(name, (c, p) -> commandConverter.apply(c));
	}

	/**
	 * Register a function to convert a composite command to a node .
	 * Here stateless means, no need for external parameters.
	 *
	 * @param name the name of the command
	 * @param commandConverter function to convert a command and a list of child nodes into a node
	 * @return the BtCommandParser
	 */
	public static final BtCommandParserDefinition statelessCompositeCommand(final String name, final BiFunction<BtCommand, List<BTNode>, BTNode> commandConverter) {
		return compositeCommand(name, (c, p, l) -> commandConverter.apply(c, l));
	}

	/**
	 * A TriFunction that resolves inputs (Command, parameters and childs) into a BTNode.
	 */
	@FunctionalInterface
	public interface CommandEvaluator {
		BTNode apply(BtCommand c, List<Object> p, List<BTNode> l);
	}

}
